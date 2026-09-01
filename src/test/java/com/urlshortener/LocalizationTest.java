package com.urlshortener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.config.RateLimitInterceptor;
import com.urlshortener.config.SecurityConfig;
import com.urlshortener.controller.UrlController;
import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.exception.GlobalExceptionHandler;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtTokenProvider;
import com.urlshortener.service.CustomUserDetailsService;
import com.urlshortener.service.MessageService;
import com.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class LocalizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UrlShortenerService urlShortenerService;

    @MockitoBean
    private MessageService messageService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @MockitoBean
    private com.urlshortener.service.ApiKeyService apiKeyService;

    @BeforeEach
    public void setup() throws Exception {
        given(rateLimitInterceptor.preHandle(any(), any(), any())).willReturn(true);
        given(messageService.getMessage("error.bad_request")).willReturn("Geçersiz istek.");
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testTurkishValidationError() throws Exception {
        ShortenRequest invalidRequest = new ShortenRequest("invalid-url-str", null, null, null);

        mockMvc.perform(post("/api/v1/urls/shorten")
                .header("Accept-Language", "tr")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.originalUrl").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testEnglishValidationError() throws Exception {
        ShortenRequest invalidRequest = new ShortenRequest("invalid-url-str", null, null, null);

        mockMvc.perform(post("/api/v1/urls/shorten")
                .header("Accept-Language", "en")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.originalUrl").exists());
    }
}
