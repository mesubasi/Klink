package com.urlshortener.config;

import com.urlshortener.security.JwtAuthenticationFilter;
import com.urlshortener.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final com.urlshortener.security.ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080,http://127.0.0.1:3000}")
    private String allowedOrigins;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          com.urlshortener.security.ApiKeyAuthenticationFilter apiKeyAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                .contentTypeOptions(org.springframework.security.config.Customizer.withDefaults())
                .referrerPolicy(referrer -> referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )
            .authorizeHttpRequests(auth -> auth
                // Public Yönlendirme, Önizleme ve Link-in-Bio Endpoint'leri
                .requestMatchers(HttpMethod.GET, "/{shortCode:[a-zA-Z0-9_-]{3,20}}", "/{shortCode:[a-zA-Z0-9_-]{3,20}}+", "/preview/**", "/bio/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/urls/*/preview", "/api/v1/urls/*/qrcode", "/api/v1/urls/analytics/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/bio/{username:[a-zA-Z0-9_-]+}").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/bio/{username:[a-zA-Z0-9_-]+}/view", "/api/v1/bio/link/*/click").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/urls/shorten", "/api/v1/urls/bulk-shorten", "/api/v1/urls/*/proceed", "/api/v1/urls/*/verify-password", "/api/v1/urls/qrcode/**").permitAll()
                // Kullanıcı Kaydı & Girişi (Register, Login, Logout, 2FA Login Verification)
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/logout", "/api/v1/auth/2fa/verify-login").permitAll()
                // Swagger UI & OpenAPI Dokümanları
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Actuator Healthcheck, H2 Konsolu ve Statik Kaynaklar
                .requestMatchers("/actuator/health", "/actuator/health/**", "/h2-console/**", "/error", "/css/**", "/js/**", "/index.html", "/").permitAll()
                
                // Admin Özel Telemetri ve CRM Endpoint'leri (Yalnızca ROLE_ADMIN erişebilir)
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                
                // Kullanıcı Yönetim Endpoint'leri (Bio Me, API Keys ve Çalışma Alanları Dahil)
                .requestMatchers("/api/v1/bio/me", "/api/v1/api-keys/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v1/urls/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v1/workspaces/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v1/auth/me", "/api/v1/auth/2fa/**").hasAnyRole("USER", "ADMIN")
                
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept-Language", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
