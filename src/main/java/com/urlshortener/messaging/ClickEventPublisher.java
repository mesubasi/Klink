package com.urlshortener.messaging;

import com.urlshortener.dto.ClickEventDto;
import com.urlshortener.model.ClickAnalytics;
import com.urlshortener.repository.ClickAnalyticsRepository;
import com.urlshortener.repository.UrlMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClickEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ClickEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final UrlMappingRepository urlMappingRepository;
    private final ClickAnalyticsRepository clickAnalyticsRepository;

    @Value("${app.rabbitmq.exchange:url.click.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key:url.click.routingKey}")
    private String routingKey;

    public ClickEventPublisher(RabbitTemplate rabbitTemplate, UrlMappingRepository urlMappingRepository, ClickAnalyticsRepository clickAnalyticsRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.urlMappingRepository = urlMappingRepository;
        this.clickAnalyticsRepository = clickAnalyticsRepository;
    }

    public void publishClickEvent(ClickEventDto event) {
        try {
            log.info("RabbitMQ'ya tıklama olayı gönderiliyor: {}", event.getShortCode());
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
        } catch (Exception e) {
            log.warn("RabbitMQ bağlantı hatası! Tıklama olayı doğrudan DB'ye işleniyor. Hata: {}", e.getMessage());
            try {
                // Bot tıklamalarında clickCount artırılmaz
                if (!event.isBot()) {
                    urlMappingRepository.incrementClickCount(event.getShortCode());
                }
                ClickAnalytics analytics = ClickAnalytics.builder()
                        .shortCode(event.getShortCode())
                        .clickedAt(event.getClickedAt())
                        .ipAddress(event.getIpAddress())
                        .userAgent(event.getUserAgent())
                        .referrer(event.getReferrer())
                        .bot(event.isBot())
                        .botCategory(event.getBotCategory())
                        .build();
                clickAnalyticsRepository.save(analytics);
            } catch (Exception ex) {
                log.error("Fallback DB işleminde hata oluştu: {}", ex.getMessage());
            }
        }
    }
}
