package com.urlshortener;

import com.urlshortener.config.RateLimitInterceptor;
import com.urlshortener.config.SecurityConfig;
import com.urlshortener.controller.SystemStatusController;
import com.urlshortener.dto.RabbitMqStatusDto;
import com.urlshortener.dto.RedisStatusDto;
import com.urlshortener.dto.SystemStatusResponse;
import com.urlshortener.exception.GlobalExceptionHandler;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtTokenProvider;
import com.urlshortener.service.CustomUserDetailsService;
import com.urlshortener.service.MessageService;
import com.urlshortener.service.SystemMonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemStatusController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class SystemStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SystemMonitoringService systemMonitoringService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private MessageService messageService;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @org.junit.jupiter.api.BeforeEach
    public void setup() throws Exception {
        given(rateLimitInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void shouldReturnSystemStatusWithRedisAndRabbitMq() throws Exception {
        RedisStatusDto redis = RedisStatusDto.builder()
                .status("CONNECTED")
                .host("localhost")
                .port(6379)
                .pingLatencyMs(2L)
                .totalKeys(35L)
                .usedMemory("1.8MB")
                .redisVersion("7.2.4")
                .uptimeDays(10L)
                .message("Redis aktif")
                .build();

        RabbitMqStatusDto rabbit = RabbitMqStatusDto.builder()
                .status("CONNECTED")
                .host("localhost")
                .port(5672)
                .virtualHost("/")
                .queueName("url.click.queue")
                .messageCount(5)
                .consumerCount(1)
                .exchangeName("url.click.exchange")
                .routingKey("url.click.routingKey")
                .message("RabbitMQ aktif, kuyrukta 5 mesaj var")
                .build();

        SystemStatusResponse response = SystemStatusResponse.builder()
                .overallStatus("HEALTHY")
                .timestamp("2026-08-19T01:55:00Z")
                .redis(redis)
                .rabbitMq(rabbit)
                .build();

        given(systemMonitoringService.getSystemStatus()).willReturn(response);

        mockMvc.perform(get("/api/v1/admin/system/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallStatus").value("HEALTHY"))
                .andExpect(jsonPath("$.redis.status").value("CONNECTED"))
                .andExpect(jsonPath("$.redis.totalKeys").value(35))
                .andExpect(jsonPath("$.rabbitMq.status").value("CONNECTED"))
                .andExpect(jsonPath("$.rabbitMq.queueName").value("url.click.queue"))
                .andExpect(jsonPath("$.rabbitMq.messageCount").value(5))
                .andExpect(jsonPath("$.rabbitMq.consumerCount").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void shouldFlushRedisCacheSuccessfully() throws Exception {
        given(systemMonitoringService.clearRedisCache()).willReturn(12L);

        mockMvc.perform(post("/api/v1/admin/system/redis/flush-cache").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.deletedKeysCount").value(12));
    }
}
