package com.urlshortener;

import com.urlshortener.dto.ClickEventDto;
import com.urlshortener.messaging.ClickEventPublisher;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.service.BotDetectorService;
import com.urlshortener.service.GeoIpService;
import com.urlshortener.service.MessageService;
import com.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedirectCacheTest {

    @Mock private UrlMappingRepository urlMappingRepository;
    @Mock private ClickEventPublisher clickEventPublisher;
    @Mock private MessageService messageService;
    @Mock private GeoIpService geoIpService;
    @Mock private BotDetectorService botDetectorService;
    @Mock private HttpServletRequest request;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "domain", "http://localhost:8080");
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(geoIpService.resolveLocation(any())).thenReturn(new GeoIpService.GeoLocation("Türkiye", "TR", "İstanbul"));
        lenient().when(botDetectorService.isBot(any())).thenReturn(false);
    }

    private UrlMapping.Builder simple() {
        return UrlMapping.builder().shortCode("promo").originalUrl("https://example.com/promo").active(true);
    }

    @Test
    void cacheHitServesRedirectWithoutDatabaseAndRecordsClick() {
        when(valueOps.get("short_url:promo")).thenReturn("https://example.com/promo?utm_source=news");

        Optional<String> result = service.resolveFromCache("promo", request);

        assertEquals(Optional.of("https://example.com/promo?utm_source=news"), result);
        verifyNoInteractions(urlMappingRepository);
        ArgumentCaptor<ClickEventDto> event = ArgumentCaptor.forClass(ClickEventDto.class);
        verify(clickEventPublisher).publishClickEvent(event.capture());
        assertEquals("promo", event.getValue().getShortCode());
        assertEquals("news", event.getValue().getUtmSource());
    }

    @Test
    void cacheMissOrRedisFailureFallsBackToDatabasePath() {
        when(valueOps.get("short_url:promo")).thenReturn(null);
        assertTrue(service.resolveFromCache("promo", request).isEmpty());

        when(valueOps.get("short_url:promo")).thenThrow(new RuntimeException("redis down"));
        assertTrue(service.resolveFromCache("promo", request).isEmpty());

        verify(clickEventPublisher, never()).publishClickEvent(any());
    }

    @Test
    void databasePathPopulatesCacheForSimpleLinks() {
        when(urlMappingRepository.findByShortCode("promo")).thenReturn(Optional.of(simple().build()));

        service.getOriginalUrlAndRecordClick("promo", request);

        verify(valueOps).set(eq("short_url:promo"), eq("https://example.com/promo"), eq(Duration.ofHours(24)));
    }

    @Test
    void cacheTtlNeverOutlivesTheLink() {
        long expiresIn = 60_000L;
        when(urlMappingRepository.findByShortCode("promo"))
                .thenReturn(Optional.of(simple().expiresAt(System.currentTimeMillis() + expiresIn).build()));

        service.getOriginalUrlAndRecordClick("promo", request);

        ArgumentCaptor<Duration> ttl = ArgumentCaptor.forClass(Duration.class);
        verify(valueOps).set(eq("short_url:promo"), anyString(), ttl.capture());
        assertTrue(ttl.getValue().toMillis() <= expiresIn && ttl.getValue().toMillis() > 0);
    }

    @Test
    void linksNeedingPerRequestDecisionsAreNeverCached() {
        UrlMapping[] restricted = {
                simple().passwordHash("hash").build(),
                simple().maxClicks(100L).build(),
                simple().previewEnabled(true).build(),
                simple().blockedCountries("DE").build(),
                simple().blockedIps("10.0.0.0/8").build(),
                simple().iosUrl("https://example.com/ios").build(),
                simple().androidUrl("https://example.com/android").build(),
                simple().desktopUrl("https://example.com/desktop").build(),
        };
        for (UrlMapping mapping : restricted) {
            assertFalse(service.isCacheable(mapping), "should not be cacheable: " + mapping);
        }

        UrlMapping abTest = simple().build();
        abTest.setAbTestingEnabled(true);
        assertFalse(service.isCacheable(abTest));

        UrlMapping inactive = simple().active(false).build();
        assertFalse(service.isCacheable(inactive));

        UrlMapping expired = simple().expiresAt(System.currentTimeMillis() - 1000).build();
        assertFalse(service.isCacheable(expired));

        assertTrue(service.isCacheable(simple().build()));
    }
}
