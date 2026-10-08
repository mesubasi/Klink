package com.urlshortener;

import com.urlshortener.exception.RateLimitExceededException;
import com.urlshortener.service.ActionRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActionRateLimiterTest {

    @SuppressWarnings("unchecked")
    @Test
    void blocksOnceTheLimitIsExceededAndStartsTheWindowOnFirstHit() {
        RedisTemplate<String, Object> redis = mock(RedisTemplate.class);
        ValueOperations<String, Object> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.increment("action_limit:register:1.2.3.4")).thenReturn(1L, 2L, 3L);
        ActionRateLimiter limiter = new ActionRateLimiter(redis);

        assertDoesNotThrow(() -> limiter.check("register", "1.2.3.4", 2, Duration.ofHours(1)));
        verify(redis).expire("action_limit:register:1.2.3.4", Duration.ofHours(1));
        assertDoesNotThrow(() -> limiter.check("register", "1.2.3.4", 2, Duration.ofHours(1)));
        assertThrows(RateLimitExceededException.class, () -> limiter.check("register", "1.2.3.4", 2, Duration.ofHours(1)));
    }

    @SuppressWarnings("unchecked")
    @Test
    void failsOpenWhenRedisIsDown() {
        RedisTemplate<String, Object> redis = mock(RedisTemplate.class);
        when(redis.opsForValue()).thenThrow(new RuntimeException("redis down"));
        ActionRateLimiter limiter = new ActionRateLimiter(redis);

        assertDoesNotThrow(() -> limiter.check("register", "ip", 1, Duration.ofHours(1)));
    }
}
