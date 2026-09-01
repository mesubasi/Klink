package com.urlshortener.config;

import com.urlshortener.exception.RateLimitExceededException;
import com.urlshortener.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.Instant;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageService messageService;

    @Value("${app.ratelimit.max-requests-per-minute:60}")
    private int maxRequestsPerMinute;

    public RateLimitInterceptor(RedisTemplate<String, Object> redisTemplate, MessageService messageService) {
        this.redisTemplate = redisTemplate;
        this.messageService = messageService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        long currentMinuteWindow = Instant.now().getEpochSecond() / 60;
        String redisKey = "rate_limit:" + clientIp + ":" + currentMinuteWindow;

        try {
            Long currentRequests = redisTemplate.opsForValue().increment(redisKey);
            if (currentRequests != null && currentRequests == 1) {
                redisTemplate.expire(redisKey, Duration.ofMinutes(1));
            }

            if (currentRequests != null && currentRequests > maxRequestsPerMinute) {
                log.warn("Rate Limit Exceeded! IP: {}, Requests: {}/min", clientIp, currentRequests);
                String errorMsg = messageService.getMessage("error.rate_limit_exceeded", maxRequestsPerMinute);
                throw new RateLimitExceededException(errorMsg);
            }
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis Rate Limiter warning (fallback applied): {}", e.getMessage());
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
