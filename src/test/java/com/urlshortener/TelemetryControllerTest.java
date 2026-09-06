package com.urlshortener;

import com.urlshortener.config.RateLimitInterceptor;
import com.urlshortener.config.SecurityConfig;
import com.urlshortener.controller.TelemetryController;
import com.urlshortener.dto.LiveClickDto;
import com.urlshortener.exception.GlobalExceptionHandler;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.security.JwtTokenProvider;
import com.urlshortener.service.ApiKeyService;
import com.urlshortener.service.CustomUserDetailsService;
import com.urlshortener.service.LiveClickStreamService;
import com.urlshortener.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TelemetryController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class TelemetryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LiveClickStreamService liveClickStreamService;

    @MockitoBean
    private UrlMappingRepository urlMappingRepository;

    @MockitoBean
    private WorkspaceMemberRepository workspaceMemberRepository;

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

    @MockitoBean
    private ApiKeyService apiKeyService;

    @BeforeEach
    public void setup() throws Exception {
        given(rateLimitInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void getStreamStatus_ShouldReturnUp() throws Exception {
        given(liveClickStreamService.getActiveSubscriberCount()).willReturn(5);

        mockMvc.perform(get("/api/v1/telemetry/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.activeSubscribers").value(5));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void getRecentClicks_ShouldReturnList() throws Exception {
        LiveClickDto click = LiveClickDto.builder()
                .shortCode("abc1234")
                .originalUrl("https://example.com")
                .country("Türkiye")
                .city("İstanbul")
                .deviceType("Masaüstü (Desktop)")
                .browser("Google Chrome")
                .os("Windows")
                .maskedIp("192.168.***.***")
                .referrer("Google")
                .clickedAt(System.currentTimeMillis())
                .bot(false)
                .build();

        given(liveClickStreamService.getRecentClicks(eq("testuser"), eq(false), isNull()))
                .willReturn(List.of(click));

        mockMvc.perform(get("/api/v1/telemetry/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortCode").value("abc1234"))
                .andExpect(jsonPath("$[0].city").value("İstanbul"))
                .andExpect(jsonPath("$[0].browser").value("Google Chrome"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void streamLiveClicks_ShouldReturnSseEmitter() throws Exception {
        SseEmitter emitter = new SseEmitter(10000L);
        given(liveClickStreamService.subscribe(eq("testuser"), eq(false), isNull()))
                .willReturn(emitter);

        mockMvc.perform(get("/api/v1/telemetry/stream")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void unauthenticatedRequest_ShouldBeDenied() throws Exception {
        mockMvc.perform(get("/api/v1/telemetry/recent"))
                .andExpect(status().isForbidden());
    }
}
