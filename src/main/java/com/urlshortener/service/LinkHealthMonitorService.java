package com.urlshortener.service;

import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class LinkHealthMonitorService {

    private static final Logger log = LoggerFactory.getLogger(LinkHealthMonitorService.class);

    private final UrlMappingRepository urlMappingRepository;
    private final MessageService messageService;
    private final HttpClient httpClient;

    public LinkHealthMonitorService(UrlMappingRepository urlMappingRepository, MessageService messageService) {
        this.urlMappingRepository = urlMappingRepository;
        this.messageService = messageService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Tek bir UrlMapping için HTTP sağlık kontrolü yapar ve sonucu veri tabanına kaydeder.
     */
    @Transactional
    public UrlMapping checkUrlHealth(UrlMapping mapping) {
        if (mapping == null || mapping.getOriginalUrl() == null || mapping.getOriginalUrl().trim().isEmpty()) {
            return mapping;
        }

        String targetUrl = mapping.getOriginalUrl().trim();
        long startTime = System.currentTimeMillis();
        String healthStatus;
        Integer statusCode = null;
        String errorMessage;
        long duration;

        try {
            URI uri = URI.create(targetUrl);
            
            // 1. Önce hızlı ve hafif olan HTTP HEAD isteğini dene
            HttpResponse<Void> response = null;
            try {
                HttpRequest headRequest = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("User-Agent", "Klink-Health-Monitor/1.0 (Link Reliability Engine)")
                        .header("Accept", "*/*")
                        .method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(6))
                        .build();

                response = httpClient.send(headRequest, HttpResponse.BodyHandlers.discarding());
            } catch (Exception headEx) {
                log.debug("HEAD isteği başarısız oldu ({}), GET ile tekrar deneniyor: {}", targetUrl, headEx.getMessage());
            }

            // 2. HEAD desteklenmiyorsa (405 Method Not Allowed) veya hata verdiyse GET isteği ile fallback yap
            if (response == null || response.statusCode() == 405) {
                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("User-Agent", "Klink-Health-Monitor/1.0 (Link Reliability Engine)")
                        .header("Accept", "*/*")
                        .GET()
                        .timeout(Duration.ofSeconds(6))
                        .build();

                response = httpClient.send(getRequest, HttpResponse.BodyHandlers.discarding());
            }

            duration = System.currentTimeMillis() - startTime;
            statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 400) {
                healthStatus = "HEALTHY";
                errorMessage = statusCode + " OK (" + duration + "ms)";
            } else if (statusCode == 401 || statusCode == 403 || statusCode == 429) {
                healthStatus = "DEGRADED";
                errorMessage = statusCode + " " + getHttpStatusDescription(statusCode) + " (" + duration + "ms)";
            } else {
                healthStatus = "BROKEN";
                errorMessage = statusCode + " " + getHttpStatusDescription(statusCode);
            }

        } catch (HttpConnectTimeoutException e) {
            duration = System.currentTimeMillis() - startTime;
            statusCode = 0;
            healthStatus = "BROKEN";
            errorMessage = "Bağlantı zaman aşımı (Timeout > 6s)";
            log.warn("Link sağlık kontrolü zaman aşımına uğradı [{}]: {}", targetUrl, e.getMessage());
        } catch (UnknownHostException e) {
            duration = System.currentTimeMillis() - startTime;
            statusCode = 0;
            healthStatus = "BROKEN";
            errorMessage = "Alan adı bulunamadı (DNS Hatası)";
            log.warn("Link DNS çözümlenemedi [{}]: {}", targetUrl, e.getMessage());
        } catch (ConnectException e) {
            duration = System.currentTimeMillis() - startTime;
            statusCode = 0;
            healthStatus = "BROKEN";
            errorMessage = "Sunucuya bağlanılamadı (Connection Refused)";
            log.warn("Link sunucusuna bağlanılamadı [{}]: {}", targetUrl, e.getMessage());
        } catch (SSLException e) {
            duration = System.currentTimeMillis() - startTime;
            statusCode = 0;
            healthStatus = "BROKEN";
            errorMessage = "SSL/TLS Sertifika Hatası";
            log.warn("Link SSL el sıkışma hatası [{}]: {}", targetUrl, e.getMessage());
        } catch (Exception e) {
            duration = System.currentTimeMillis() - startTime;
            statusCode = 0;
            healthStatus = "BROKEN";
            errorMessage = "Erişim hatası: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            log.warn("Link sağlık kontrolünde beklenmeyen hata [{}]: {}", targetUrl, e.getMessage());
        }

        mapping.setHealthStatus(healthStatus);
        mapping.setLastHealthCheck(System.currentTimeMillis());
        mapping.setHealthStatusCode(statusCode);
        mapping.setHealthErrorMessage(errorMessage);
        mapping.setHealthResponseTimeMs(duration);

        return urlMappingRepository.save(mapping);
    }

    /**
     * Belirli bir kısa kod için anlık sağlık kontrolü gerçekleştirir.
     */
    @Transactional
    public UrlMapping checkHealthByShortCode(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(messageService.getMessage("url.not_found", shortCode)));

        return checkUrlHealth(mapping);
    }

    /**
     * Belirli bir kullanıcının tüm aktif linklerinin sağlığını toplu olarak kontrol eder.
     */
    @Transactional
    public List<UrlMapping> checkUserUrlsHealth(String username) {
        List<UrlMapping> userUrls = urlMappingRepository.findByUserUsername(username);
        List<UrlMapping> updatedList = new ArrayList<>();

        for (UrlMapping mapping : userUrls) {
            if (mapping.isActive()) {
                updatedList.add(checkUrlHealth(mapping));
            } else {
                updatedList.add(mapping);
            }
        }

        return updatedList;
    }

    /**
     * Arka planda periyodik olarak tüm aktif linkleri tarar (15 dakikada bir).
     */
    @Scheduled(fixedRate = 900000) // 15 dakika (900,000 ms)
    @Transactional
    public void checkAllUrlsHealthScheduled() {
        List<UrlMapping> activeUrls = urlMappingRepository.findByActiveTrue();
        if (activeUrls.isEmpty()) {
            return;
        }

        log.info("🏥 [Link Health Monitor] Periyodik sağlık kontrolü başladı. Toplam aktif link: {}", activeUrls.size());
        int healthyCount = 0;
        int brokenCount = 0;
        int degradedCount = 0;

        for (UrlMapping mapping : activeUrls) {
            UrlMapping checked = checkUrlHealth(mapping);
            if ("HEALTHY".equals(checked.getHealthStatus())) {
                healthyCount++;
            } else if ("BROKEN".equals(checked.getHealthStatus())) {
                brokenCount++;
            } else if ("DEGRADED".equals(checked.getHealthStatus())) {
                degradedCount++;
            }
        }

        log.info("🏥 [Link Health Monitor] Sağlık kontrolü tamamlandı -> Sağlıklı: {}, Kırık: {}, Kısıtlı: {}", 
                healthyCount, brokenCount, degradedCount);
    }

    private String getHttpStatusDescription(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 307 -> "Temporary Redirect";
            case 308 -> "Permanent Redirect";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 408 -> "Request Timeout";
            case 410 -> "Gone";
            case 429 -> "Too Many Requests";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            default -> "HTTP " + statusCode;
        };
    }
}
