package com.urlshortener;

import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.messaging.ClickEventPublisher;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.service.MessageService;
import com.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClickCapTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private ClickEventPublisher clickEventPublisher;

    @Mock
    private MessageService messageService;

    @Mock
    private com.urlshortener.service.GeoIpService geoIpService;

    @Mock
    private com.urlshortener.service.BotDetectorService botDetectorService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(urlShortenerService, "domain", "http://localhost:8080");
        lenient().when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(messageService.getMessage(eq("url.click_limit_reached"))).thenReturn("Bu linkin maksimum tıklama sınırına ulaşılmıştır.");
        lenient().when(geoIpService.resolveLocation(any())).thenReturn(new com.urlshortener.service.GeoIpService.GeoLocation("Türkiye", "TR", "İstanbul"));
        lenient().when(botDetectorService.isBot(any())).thenReturn(false);
    }

    @Test
    public void testClickUnderLimitRedirectsToOriginal() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("promo")
                .originalUrl("https://example.com/promo")
                .clickCount(5L)
                .maxClicks(10L)
                .active(true)
                .build();

        when(urlMappingRepository.findByShortCode("promo")).thenReturn(Optional.of(mapping));

        String result = urlShortenerService.getOriginalUrlAndRecordClick("promo", httpServletRequest);

        assertEquals("https://example.com/promo", result);
        verify(clickEventPublisher, times(1)).publishClickEvent(any());
    }

    @Test
    public void testClickLimitReachedRedirectsToFallbackUrl() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("promo")
                .originalUrl("https://example.com/promo")
                .fallbackUrl("https://example.com/quota-full")
                .clickCount(10L)
                .maxClicks(10L)
                .active(true)
                .build();

        when(urlMappingRepository.findByShortCode("promo")).thenReturn(Optional.of(mapping));

        String result = urlShortenerService.getOriginalUrlAndRecordClick("promo", httpServletRequest);

        assertEquals("https://example.com/quota-full", result);
        verify(clickEventPublisher, times(1)).publishClickEvent(any());
    }

    @Test
    public void testClickLimitReachedWithoutFallbackThrowsException() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("promo")
                .originalUrl("https://example.com/promo")
                .fallbackUrl(null)
                .clickCount(10L)
                .maxClicks(10L)
                .active(true)
                .build();

        when(urlMappingRepository.findByShortCode("promo")).thenReturn(Optional.of(mapping));

        UrlNotFoundException ex = assertThrows(UrlNotFoundException.class, () -> {
            urlShortenerService.getOriginalUrlAndRecordClick("promo", httpServletRequest);
        });

        assertEquals("Bu linkin maksimum tıklama sınırına ulaşılmıştır.", ex.getMessage());
    }
}
