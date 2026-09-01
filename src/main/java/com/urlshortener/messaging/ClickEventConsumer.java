package com.urlshortener.messaging;

import com.urlshortener.dto.ClickEventDto;
import com.urlshortener.model.ClickAnalytics;
import com.urlshortener.repository.ClickAnalyticsRepository;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.service.WebhookDispatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ClickEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClickEventConsumer.class);

    private final ClickAnalyticsRepository clickAnalyticsRepository;
    private final UrlMappingRepository urlMappingRepository;
    private final WebhookDispatcherService webhookDispatcherService;

    public ClickEventConsumer(ClickAnalyticsRepository clickAnalyticsRepository,
                              UrlMappingRepository urlMappingRepository,
                              WebhookDispatcherService webhookDispatcherService) {
        this.clickAnalyticsRepository = clickAnalyticsRepository;
        this.urlMappingRepository = urlMappingRepository;
        this.webhookDispatcherService = webhookDispatcherService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue:url.click.queue}")
    @Transactional
    public void consumeClickEvent(ClickEventDto clickEvent) {
        log.info("RabbitMQ Tıklama Olayı Alındı! ShortCode: {}, IP: {}, Ülke: {}, Şehir: {}, Bot: {}", 
                clickEvent.getShortCode(), clickEvent.getIpAddress(), clickEvent.getCountry(), clickEvent.getCity(), clickEvent.isBot());

        try {
            ClickAnalytics analytics = ClickAnalytics.builder()
                    .shortCode(clickEvent.getShortCode())
                    .clickedAt(clickEvent.getClickedAt())
                    .ipAddress(clickEvent.getIpAddress())
                    .userAgent(clickEvent.getUserAgent())
                    .referrer(clickEvent.getReferrer())
                    .country(clickEvent.getCountry() != null ? clickEvent.getCountry() : "Türkiye (Turkey)")
                    .countryCode(clickEvent.getCountryCode() != null ? clickEvent.getCountryCode() : "TR")
                    .city(clickEvent.getCity() != null ? clickEvent.getCity() : "İstanbul")
                    .bot(clickEvent.isBot())
                    .botCategory(clickEvent.getBotCategory())
                    .build();

            clickAnalyticsRepository.save(analytics);

            // Bot tıklamaları clickCount sayacını artırmaz — sadece gerçek kullanıcı tıklamaları sayılır
            if (!clickEvent.isBot()) {
                urlMappingRepository.incrementClickCount(clickEvent.getShortCode());
            } else {
                log.info("Bot tıklaması tespit edildi ({}), clickCount artırılmadı: {}", 
                        clickEvent.getBotCategory(), clickEvent.getShortCode());
            }

            // Webhook gönderimini asenkron tetikle
            webhookDispatcherService.dispatchClickWebhook(clickEvent);
        } catch (Exception e) {
            log.error("Tıklama olayı işlenirken veritabanı hatası: {}", e.getMessage(), e);
        }
    }
}
