package com.urlshortener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.config.RateLimitInterceptor;
import com.urlshortener.config.SecurityConfig;
import com.urlshortener.controller.AuthController;
import com.urlshortener.dto.AuthResponse;
import com.urlshortener.dto.RegisterRequest;
import com.urlshortener.dto.UserDto;
import com.urlshortener.exception.GlobalExceptionHandler;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtTokenProvider;
import com.urlshortener.service.AuthService;
import com.urlshortener.service.CustomUserDetailsService;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

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
        given(messageService.getMessage(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(messageService.getMessage(any(), any())).willAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest("yeni_kullanici", "kullanici@example.com", "password123");
        AuthResponse response = AuthResponse.builder()
                .username("yeni_kullanici")
                .email("kullanici@example.com")
                .role("ROLE_USER")
                .message("Kullanıcı kaydı başarıyla oluşturuldu!")
                .build();

        given(authService.register(any(RegisterRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("yeni_kullanici"))
                .andExpect(jsonPath("$.email").value("kullanici@example.com"));
    }

    @Test
    @WithMockUser(username = "yeni_kullanici", roles = "USER")
    public void testGetCurrentUserSuccess() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(java.util.UUID.randomUUID())
                .username("yeni_kullanici")
                .email("kullanici@example.com")
                .role("ROLE_USER")
                .createdAt(System.currentTimeMillis())
                .build();

        given(authService.getCurrentUser("yeni_kullanici")).willReturn(userDto);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("yeni_kullanici"));
    }
}
