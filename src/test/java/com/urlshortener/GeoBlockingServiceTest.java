package com.urlshortener;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.exception.UrlAccessRestrictedException;
import com.urlshortener.messaging.ClickEventPublisher;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.ClickAnalyticsRepository;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.service.GeoIpService;
import com.urlshortener.service.MessageService;
import com.urlshortener.service.ReportExportService;
import com.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GeoBlockingServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private ClickAnalyticsRepository clickAnalyticsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClickEventPublisher clickEventPublisher;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MessageService messageService;

    @Mock
    private GeoIpService geoIpService;

    @Mock
    private ReportExportService reportExportService;

    @Mock
    private com.urlshortener.service.UrlSecurityScannerService urlSecurityScannerService;

    @Mock
    private com.urlshortener.service.BotDetectorService botDetectorService;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    @BeforeEach
    public void setup() {
        given(messageService.getMessage(any())).willAnswer(inv -> inv.getArgument(0));
    }

    @Test
    public void testShortenUrlWithBlockedCountriesAndIps() {
        ShortenRequest request = new ShortenRequest("https://example.com", "customGeo", null, null, "https://fallback.com", "US,RU", "8.8.8.8,10.0.0.0/8");
        given(urlMappingRepository.existsByShortCode("customGeo")).willReturn(false);
        given(urlMappingRepository.save(any(UrlMapping.class))).willAnswer(inv -> inv.getArgument(0));

        ShortenResponse response = urlShortenerService.shortenUrl(request);

        assertNotNull(response);
        assertEquals("US,RU", response.getBlockedCountries());
        assertEquals("8.8.8.8,10.0.0.0/8", response.getBlockedIps());
    }

    @Test
    public void testAccessBlockedByCountryWithoutFallbackThrowsException() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("blockedLink")
                .originalUrl("https://target.com")
                .active(true)
                .blockedCountries("US")
                .build();

        given(urlMappingRepository.findByShortCode("blockedLink")).willReturn(Optional.of(mapping));

        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("X-Forwarded-For")).willReturn("8.8.8.8");
        given(geoIpService.resolveLocation("8.8.8.8")).willReturn(new GeoIpService.GeoLocation("United States", "US", "Mountain View"));

        assertThrows(UrlAccessRestrictedException.class, () -> {
            urlShortenerService.getOriginalUrlAndRecordClick("blockedLink", request);
        });
    }

    @Test
    public void testAccessBlockedByCountryWithFallbackReturnsFallbackUrl() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("blockedLinkWithFallback")
                .originalUrl("https://target.com")
                .fallbackUrl("https://fallback.com")
                .active(true)
                .blockedCountries("US")
                .build();

        given(urlMappingRepository.findByShortCode("blockedLinkWithFallback")).willReturn(Optional.of(mapping));

        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("X-Forwarded-For")).willReturn("8.8.8.8");
        given(geoIpService.resolveLocation("8.8.8.8")).willReturn(new GeoIpService.GeoLocation("United States", "US", "Mountain View"));

        String resultUrl = urlShortenerService.getOriginalUrlAndRecordClick("blockedLinkWithFallback", request);
        assertEquals("https://fallback.com", resultUrl);
    }

    @Test
    public void testAccessAllowedCountryAndIpReturnsOriginalUrl() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("allowedLink")
                .originalUrl("https://target.com")
                .active(true)
                .blockedCountries("US")
                .blockedIps("1.1.1.1")
                .build();

        given(urlMappingRepository.findByShortCode("allowedLink")).willReturn(Optional.of(mapping));

        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("X-Forwarded-For")).willReturn("185.10.10.10");
        given(geoIpService.resolveLocation("185.10.10.10")).willReturn(new GeoIpService.GeoLocation("Turkey", "TR", "Istanbul"));

        String resultUrl = urlShortenerService.getOriginalUrlAndRecordClick("allowedLink", request);
        assertEquals("https://target.com", resultUrl);
    }
}
