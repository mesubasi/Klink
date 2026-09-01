package com.urlshortener.security;

import com.urlshortener.model.ApiKey;
import com.urlshortener.service.ApiKeyService;
import com.urlshortener.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;
    private final CustomUserDetailsService userDetailsService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService, CustomUserDetailsService userDetailsService) {
        this.apiKeyService = apiKeyService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String apiKeyHeader = request.getHeader("X-API-KEY");
            if (!StringUtils.hasText(apiKeyHeader)) {
                apiKeyHeader = request.getHeader("x-api-key");
            }

            if (StringUtils.hasText(apiKeyHeader) && SecurityContextHolder.getContext().getAuthentication() == null) {
                ApiKey apiKey = apiKeyService.authenticateApiKey(apiKeyHeader.trim());
                if (apiKey != null && apiKey.getUser() != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(apiKey.getUser().getUsername());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("API Key kimlik doğrulaması sırasında hata: ", ex);
        }

        filterChain.doFilter(request, response);
    }
}
