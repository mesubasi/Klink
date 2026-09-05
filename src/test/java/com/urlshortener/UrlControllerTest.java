package com.urlshortener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.config.RateLimitInterceptor;
import com.urlshortener.config.SecurityConfig;
import com.urlshortener.controller.UrlController;
import com.urlshortener.dto.*;
import com.urlshortener.exception.GlobalExceptionHandler;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtTokenProvider;
import com.urlshortener.service.CustomUserDetailsService;
import com.urlshortener.service.GeoIpService;
import com.urlshortener.service.MessageService;
import com.urlshortener.service.ReportExportService;
import com.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UrlShortenerService urlShortenerService;

    @MockitoBean
    private MessageService messageService;

    @MockitoBean
    private GeoIpService geoIpService;

    @MockitoBean
    private ReportExportService reportExportService;

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
    @WithMockUser(roles = "USER")
    public void testShortenUrlSuccess() throws Exception {
        ShortenRequest request = new ShortenRequest("https://google.com", "mygoogle", 7, "secret123");
        ShortenResponse response = ShortenResponse.builder()
                .shortCode("mygoogle")
                .shortUrl("http://localhost:8080/mygoogle")
                .originalUrl("https://google.com")
                .createdAt(System.currentTimeMillis())
                .clickCount(0L)
                .passwordProtected(true)
                .build();

        given(urlShortenerService.shortenUrl(any(ShortenRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/urls/shorten")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("mygoogle"))
                .andExpect(jsonPath("$.passwordProtected").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testBulkShortenUrlsSuccess() throws Exception {
        ShortenRequest req1 = new ShortenRequest("https://github.com", null, null, null);
        ShortenRequest req2 = new ShortenRequest("https://stackoverflow.com", null, null, null);
        BulkShortenRequest bulkRequest = new BulkShortenRequest(List.of(req1, req2));

        ShortenResponse res1 = ShortenResponse.builder().shortCode("code1").originalUrl("https://github.com").build();
        ShortenResponse res2 = ShortenResponse.builder().shortCode("code2").originalUrl("https://stackoverflow.com").build();
        BulkShortenResponse bulkResponse = BulkShortenResponse.builder()
                .totalCount(2)
                .successCount(2)
                .shortenedUrls(List.of(res1, res2))
                .build();

        given(urlShortenerService.bulkShortenUrls(any(BulkShortenRequest.class))).willReturn(bulkResponse);

        mockMvc.perform(post("/api/v1/urls/bulk-shorten")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.shortenedUrls[0].shortCode").value("code1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testVerifyPasswordSuccess() throws Exception {
        PasswordVerifyRequest request = new PasswordVerifyRequest("secret123");
        given(urlShortenerService.verifyPasswordAndGetUrl(eq("mygoogle"), eq("secret123"), any(HttpServletRequest.class)))
                .willReturn("https://google.com");

        mockMvc.perform(post("/api/v1/urls/mygoogle/verify-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("https://google.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAnalyticsSummarySuccess() throws Exception {
        AnalyticsSummaryResponse response = AnalyticsSummaryResponse.builder()
                .shortCode("mygoogle")
                .originalUrl("https://google.com")
                .totalClicks(10L)
                .clicksByDevice(Map.of("Mobil (Mobile)", 7L, "Masaüstü (Desktop)", 3L))
                .clicksByReferrer(Map.of("Instagram", 6L, "Doğrudan (Direct)", 4L))
                .clicksByDate(Map.of("2026-08-17", 10L))
                .clicksByCountry(Map.of("Türkiye (Turkey)", 10L))
                .clicksByCity(Map.of("İstanbul", 10L))
                .build();

        given(urlShortenerService.getAnalyticsSummary(eq("mygoogle"))).willReturn(response);

        mockMvc.perform(get("/api/v1/urls/analytics/mygoogle/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClicks").value(10))
                .andExpect(jsonPath("$.clicksByDevice['Mobil (Mobile)']").value(7));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testExportAnalyticsReportSuccess() throws Exception {
        byte[] fakeCsv = "Header1,Header2\nValue1,Value2".getBytes();
        given(urlShortenerService.exportAnalyticsReport(eq("mygoogle"), eq("csv"))).willReturn(fakeCsv);

        mockMvc.perform(get("/api/v1/urls/analytics/mygoogle/export?format=csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"klink-analytics-mygoogle.csv\""))
                .andExpect(content().bytes(fakeCsv));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testToggleUrlStatusSuccess() throws Exception {
        ShortenResponse response = ShortenResponse.builder()
                .shortCode("mygoogle")
                .shortUrl("http://localhost:8080/mygoogle")
                .originalUrl("https://google.com")
                .clickCount(5L)
                .build();

        given(urlShortenerService.toggleUrlStatus(eq("mygoogle"), eq(false))).willReturn(response);

        mockMvc.perform(patch("/api/v1/urls/mygoogle/status")
                .with(csrf())
                .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("mygoogle"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetQrCodeSuccess() throws Exception {
        byte[] fakeImage = new byte[]{1, 2, 3, 4};
        given(urlShortenerService.generateQrCodeForUrl(eq("mygoogle"), anyInt(), anyInt())).willReturn(fakeImage);

        mockMvc.perform(get("/api/v1/urls/mygoogle/qrcode"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE));
    }

    @Test
    public void testUrlPreviewSuccess() throws Exception {
        UrlPreviewResponse previewResponse = UrlPreviewResponse.builder()
                .shortCode("previewCode")
                .shortUrl("http://localhost:8080/previewCode")
                .originalUrl("https://example.com/some/path")
                .domain("example.com")
                .protocol("https:")
                .secure(true)
                .safetyStatus("SAFE")
                .safetyScore(98)
                .googleSafeBrowsingStatus("CLEAN")
                .virusTotalStatus("CLEAN")
                .passwordProtected(false)
                .previewEnabled(true)
                .clickCount(15L)
                .active(true)
                .build();

        given(urlShortenerService.getUrlPreview(eq("previewCode"))).willReturn(previewResponse);

        mockMvc.perform(get("/api/v1/urls/previewCode/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("previewCode"))
                .andExpect(jsonPath("$.domain").value("example.com"))
                .andExpect(jsonPath("$.safetyStatus").value("SAFE"))
                .andExpect(jsonPath("$.safetyScore").value(98))
                .andExpect(jsonPath("$.secure").value(true));
    }

    @Test
    public void testProceedFromPreviewSuccess() throws Exception {
        given(urlShortenerService.proceedFromPreview(eq("previewCode"), any(HttpServletRequest.class)))
                .willReturn("https://example.com/target");

        mockMvc.perform(post("/api/v1/urls/previewCode/proceed")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/target"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testCheckHealthSuccess() throws Exception {
        ShortenResponse response = ShortenResponse.builder()
                .shortCode("healthCode")
                .shortUrl("http://localhost:8080/healthCode")
                .originalUrl("https://example.com")
                .healthStatus("HEALTHY")
                .healthStatusCode(200)
                .healthErrorMessage("200 OK (55ms)")
                .healthResponseTimeMs(55L)
                .build();

        given(urlShortenerService.checkHealth(eq("healthCode"))).willReturn(response);

        mockMvc.perform(post("/api/v1/urls/healthCode/health-check")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("healthCode"))
                .andExpect(jsonPath("$.healthStatus").value("HEALTHY"))
                .andExpect(jsonPath("$.healthStatusCode").value(200));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testShortenUrlWithMaxClicksAndFallback() throws Exception {
        ShortenRequest request = new ShortenRequest();
        request.setOriginalUrl("https://example.com/promo");
        request.setMaxClicks(100L);
        request.setFallbackUrl("https://example.com/fallback");

        ShortenResponse response = ShortenResponse.builder()
                .shortCode("promo100")
                .shortUrl("http://localhost:8080/promo100")
                .originalUrl("https://example.com/promo")
                .maxClicks(100L)
                .fallbackUrl("https://example.com/fallback")
                .clickCount(0L)
                .build();

        given(urlShortenerService.shortenUrl(any(ShortenRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/urls/shorten")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("promo100"))
                .andExpect(jsonPath("$.maxClicks").value(100))
                .andExpect(jsonPath("$.fallbackUrl").value("https://example.com/fallback"));
    }
}
