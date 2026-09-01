package com.urlshortener.service;

import com.urlshortener.exception.MaliciousUrlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class UrlSecurityScannerService {

    private static final Logger log = LoggerFactory.getLogger(UrlSecurityScannerService.class);

    private final RestTemplate restTemplate;
    private final MessageService messageService;

    @Value("${app.security.scanner.enabled:true}")
    private boolean enabled;

    @Value("${app.security.scanner.google-safe-browsing-api-key:}")
    private String googleApiKey;

    @Value("${app.security.scanner.virustotal-api-key:}")
    private String virusTotalApiKey;

    private static final List<String> KNOWN_THREAT_DOMAINS = Arrays.asList(
            "testsafebrowsing.appspot.com",
            "phishing.test",
            "malware.testing.google.test",
            "badurl.com",
            "malicious-phishing.com",
            "eicar.org"
    );

    public UrlSecurityScannerService(RestTemplate restTemplate, MessageService messageService) {
        this.restTemplate = restTemplate;
        this.messageService = messageService;
    }

    public void checkUrlSafety(String url) {
        if (!enabled || url == null || url.trim().isEmpty()) {
            return;
        }

        String targetUrl = url.trim();

        // 1. Local / Offline Threat Pattern Check
        checkLocalThreatPatterns(targetUrl);

        // 2. Google Safe Browsing API v4 Check (if API Key is configured)
        if (googleApiKey != null && !googleApiKey.trim().isEmpty()) {
            checkGoogleSafeBrowsing(targetUrl);
        }

        // 3. VirusTotal API v3 Check (if API Key is configured)
        if (virusTotalApiKey != null && !virusTotalApiKey.trim().isEmpty()) {
            checkVirusTotal(targetUrl);
        }
    }

    private void checkLocalThreatPatterns(String url) {
        String lowerUrl = url.toLowerCase();
        for (String threatDomain : KNOWN_THREAT_DOMAINS) {
            if (lowerUrl.contains(threatDomain)) {
                log.warn("Zararlı URL yerel güvenlik filtresine takıldı: {}", url);
                throw new MaliciousUrlException(messageService.getMessage("url.malicious_detected"));
            }
        }
    }

    private void checkGoogleSafeBrowsing(String url) {
        try {
            String apiUrl = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" + googleApiKey.trim();

            Map<String, Object> clientMap = new HashMap<>();
            clientMap.put("clientId", "url-shortener");
            clientMap.put("clientVersion", "1.0.0");

            Map<String, Object> threatInfo = new HashMap<>();
            threatInfo.put("threatTypes", Arrays.asList("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"));
            threatInfo.put("platformTypes", Collections.singletonList("ANY_PLATFORM"));
            threatInfo.put("threatEntryTypes", Collections.singletonList("URL"));
            threatInfo.put("threatEntries", Collections.singletonList(Collections.singletonMap("url", url)));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("client", clientMap);
            requestBody.put("threatInfo", threatInfo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                if (response.getBody().containsKey("matches")) {
                    log.warn("Google Safe Browsing zararlı URL tespit etti: {}", url);
                    throw new MaliciousUrlException(messageService.getMessage("url.malicious_detected"));
                }
            }
        } catch (MaliciousUrlException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google Safe Browsing API sorgusu sırasında hata oluştu: {}", e.getMessage());
        }
    }

    private void checkVirusTotal(String url) {
        try {
            String encodedUrl = Base64.getUrlEncoder().withoutPadding().encodeToString(url.getBytes());
            String apiUrl = "https://www.virustotal.com/api/v3/urls/" + encodedUrl;

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-apikey", virusTotalApiKey.trim());

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map data = (Map) response.getBody().get("data");
                if (data != null && data.containsKey("attributes")) {
                    Map attributes = (Map) data.get("attributes");
                    if (attributes != null && attributes.containsKey("last_analysis_stats")) {
                        Map stats = (Map) attributes.get("last_analysis_stats");
                        Integer malicious = (Integer) stats.getOrDefault("malicious", 0);
                        if (malicious != null && malicious > 0) {
                            log.warn("VirusTotal zararlı URL tespit etti ({}/90 engine): {}", malicious, url);
                            throw new MaliciousUrlException(messageService.getMessage("url.malicious_detected"));
                        }
                    }
                }
            }
        } catch (MaliciousUrlException e) {
            throw e;
        } catch (Exception e) {
            log.error("VirusTotal API sorgusu sırasında hata oluştu: {}", e.getMessage());
        }
    }
}
