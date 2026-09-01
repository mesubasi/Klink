package com.urlshortener.service;

import com.urlshortener.dto.ClickEventDto;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

@Service
public class WebhookDispatcherService {

    private static final Logger log = LoggerFactory.getLogger(WebhookDispatcherService.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final UrlMappingRepository urlMappingRepository;

    public WebhookDispatcherService(ObjectMapper objectMapper, UrlMappingRepository urlMappingRepository) {
        this.objectMapper = objectMapper;
        this.urlMappingRepository = urlMappingRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Async
    public void dispatchClickWebhook(ClickEventDto clickEvent) {
        if (clickEvent == null || clickEvent.getShortCode() == null) {
            return;
        }

        try {
            UrlMapping mapping = urlMappingRepository.findByShortCode(clickEvent.getShortCode()).orElse(null);
            if (mapping == null || mapping.getWebhookUrl() == null || mapping.getWebhookUrl().trim().isEmpty()) {
                return;
            }

            String webhookUrl = mapping.getWebhookUrl().trim();

            Map<String, Object> payload = new HashMap<>();
            payload.put("event", "link.clicked");
            payload.put("shortCode", mapping.getShortCode());
            payload.put("originalUrl", mapping.getOriginalUrl());
            payload.put("clickedAt", clickEvent.getClickedAt());
            payload.put("ipAddress", clickEvent.getIpAddress());
            payload.put("country", clickEvent.getCountry());
            payload.put("city", clickEvent.getCity());
            payload.put("userAgent", clickEvent.getUserAgent());
            payload.put("referrer", clickEvent.getReferrer());
            payload.put("isBot", clickEvent.isBot());
            payload.put("botCategory", clickEvent.getBotCategory());

            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(4))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Klink-Webhook-Engine/1.0")
                    .header("X-Klink-Event", "link.clicked")
                    .header("X-Klink-Delivery", String.valueOf(System.currentTimeMillis()));

            if (mapping.getWebhookSecret() != null && !mapping.getWebhookSecret().trim().isEmpty()) {
                String signature = calculateHmacSha256(jsonPayload, mapping.getWebhookSecret().trim());
                reqBuilder.header("X-Klink-Signature", "sha256=" + signature);
            }

            HttpRequest request = reqBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8)).build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            log.info("⚡ [Webhook] Başarıyla iletildi (HTTP {}): {} -> {}", response.statusCode(), mapping.getShortCode(), webhookUrl);
                        } else {
                            log.warn("⚠️ [Webhook] Hedef sunucu durum kodu (HTTP {}): {} -> {}", response.statusCode(), mapping.getShortCode(), webhookUrl);
                        }
                    })
                    .exceptionally(ex -> {
                        log.warn("⚠️ [Webhook] İletim hatası ({}): {}", webhookUrl, ex.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            log.error("Webhook hazırlığı sırasında hata: {}", e.getMessage());
        }
    }

    private String calculateHmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hmacBytes);
        } catch (Exception e) {
            log.warn("HMAC-SHA256 imza üretilemedi: {}", e.getMessage());
            return "";
        }
    }
}
