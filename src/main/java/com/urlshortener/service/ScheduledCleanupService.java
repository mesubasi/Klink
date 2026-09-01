package com.urlshortener.service;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScheduledCleanupService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledCleanupService.class);

    private final UrlMappingRepository urlMappingRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public ScheduledCleanupService(UrlMappingRepository urlMappingRepository, RedisTemplate<String, Object> redisTemplate) {
        this.urlMappingRepository = urlMappingRepository;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 300000) // Runs every 5 minutes (300,000 ms)
    @Transactional
    public void cleanupExpiredUrls() {
        Long now = System.currentTimeMillis();
        List<UrlMapping> expiredUrls = urlMappingRepository.findByActiveTrueAndExpiresAtBefore(now);

        if (!expiredUrls.isEmpty()) {
            log.info("Zamanlanmış Temizlik Devrede: {} adet süresi dolmuş link tespit edildi ve pasife alınıyor...", expiredUrls.size());
            for (UrlMapping mapping : expiredUrls) {
                mapping.setActive(false);
                urlMappingRepository.save(mapping);
                try {
                    redisTemplate.delete("short_url:" + mapping.getShortCode());
                } catch (Exception e) {
                    log.warn("Redis temizleme uyarısı: {}", e.getMessage());
                }
            }
            log.info("Zamanlanmış Temizlik Tamamlandı. Pasife alınan linkler Redis önbelleğinden temizlendi.");
        }
    }
}
