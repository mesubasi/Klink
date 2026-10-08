package com.urlshortener.service;

import com.urlshortener.exception.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Fixed-window limiter for sensitive actions (registration, password reset, ...) on top of the global
 * per-IP limiter. Like that limiter it fails open when Redis is unavailable.
 */
@Component
public class ActionRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(ActionRateLimiter.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public ActionRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** Counts one attempt and throws once {@code max} attempts per {@code window} are exceeded. */
    public void check(String action, String key, int max, Duration window) {
        String redisKey = "action_limit:" + action + ":" + key;
        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count != null && count == 1) {
                redisTemplate.expire(redisKey, window);
            }
            if (count != null && count > max) {
                log.warn("Action rate limit exceeded: action={} key={} count={}", action, key, count);
                throw new RateLimitExceededException("Çok fazla deneme yaptınız. Lütfen daha sonra tekrar deneyin.");
            }
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Action rate limiter unavailable (fail-open): {}", e.getMessage());
        }
    }
}
