package com.urlshortener.service;

import com.urlshortener.dto.RabbitMqStatusDto;
import com.urlshortener.dto.RedisStatusDto;
import com.urlshortener.dto.SystemStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Properties;
import java.util.Set;

@Service
public class SystemMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(SystemMonitoringService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.rabbitmq.host:localhost}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.port:5672}")
    private int rabbitPort;

    @Value("${spring.rabbitmq.virtual-host:/}")
    private String rabbitVirtualHost;

    @Value("${app.rabbitmq.queue:url.click.queue}")
    private String queueName;

    @Value("${app.rabbitmq.exchange:url.click.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key:url.click.routingKey}")
    private String routingKey;

    public SystemMonitoringService(RedisTemplate<String, Object> redisTemplate,
                                   RabbitAdmin rabbitAdmin,
                                   RabbitTemplate rabbitTemplate) {
        this.redisTemplate = redisTemplate;
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public SystemStatusResponse getSystemStatus() {
        RedisStatusDto redisStatus = getRedisStatus();
        RabbitMqStatusDto rabbitMqStatus = getRabbitMqStatus();

        String overallStatus = "HEALTHY";
        boolean redisOk = "CONNECTED".equalsIgnoreCase(redisStatus.getStatus());
        boolean rabbitOk = "CONNECTED".equalsIgnoreCase(rabbitMqStatus.getStatus());

        if (!redisOk && !rabbitOk) {
            overallStatus = "DOWN";
        } else if (!redisOk || !rabbitOk) {
            overallStatus = "DEGRADED";
        }

        return SystemStatusResponse.builder()
                .overallStatus(overallStatus)
                .timestamp(Instant.now().toString())
                .redis(redisStatus)
                .rabbitMq(rabbitMqStatus)
                .build();
    }

    public RedisStatusDto getRedisStatus() {
        try {
            long start = System.currentTimeMillis();
            String pong = redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
            long latency = System.currentTimeMillis() - start;

            Long totalKeys = redisTemplate.execute((RedisCallback<Long>) connection -> connection.serverCommands().dbSize());
            Properties info = redisTemplate.execute((RedisCallback<Properties>) connection -> connection.serverCommands().info());

            String version = "N/A";
            String usedMemory = "N/A";
            Long uptimeDays = 0L;

            if (info != null) {
                version = info.getProperty("redis_version", "N/A");
                usedMemory = info.getProperty("used_memory_human", "N/A");
                String uptimeStr = info.getProperty("uptime_in_days");
                if (uptimeStr != null) {
                    try {
                        uptimeDays = Long.parseLong(uptimeStr);
                    } catch (NumberFormatException ignored) {}
                }
            }

            return RedisStatusDto.builder()
                    .status("CONNECTED")
                    .host(redisHost)
                    .port(redisPort)
                    .pingLatencyMs(latency)
                    .totalKeys(totalKeys != null ? totalKeys : 0L)
                    .usedMemory(usedMemory)
                    .redisVersion(version)
                    .uptimeDays(uptimeDays)
                    .message("Redis önbellek sunucusu aktif ve yanıt veriyor (PONG: " + pong + ")")
                    .build();

        } catch (Exception e) {
            log.warn("Redis bağlantı kontrolünde hata: {}", e.getMessage());
            return RedisStatusDto.builder()
                    .status("DISCONNECTED")
                    .host(redisHost)
                    .port(redisPort)
                    .pingLatencyMs(null)
                    .totalKeys(0L)
                    .usedMemory("0 B")
                    .redisVersion("N/A")
                    .uptimeDays(0L)
                    .message("Redis bağlantısı kurulamadı: " + (e.getMessage() != null ? e.getMessage() : "Sunucuya ulaşılamıyor"))
                    .build();
        }
    }

    public RabbitMqStatusDto getRabbitMqStatus() {
        try {
            QueueInformation queueInfo = rabbitAdmin.getQueueInfo(queueName);

            if (queueInfo != null) {
                int messageCount = queueInfo.getMessageCount();
                int consumerCount = queueInfo.getConsumerCount();

                return RabbitMqStatusDto.builder()
                        .status("CONNECTED")
                        .host(rabbitHost)
                        .port(rabbitPort)
                        .virtualHost(rabbitVirtualHost)
                        .queueName(queueName)
                        .messageCount(messageCount)
                        .consumerCount(consumerCount)
                        .exchangeName(exchangeName)
                        .routingKey(routingKey)
                        .message("RabbitMQ broker aktif. Kuyrukta " + messageCount + " bekleyen mesaj, " + consumerCount + " aktif tüketici var.")
                        .build();
            } else {
                // Kuyruk henüz broker üzerinde oluşmamış olabilir, test bağlantısı deneyelim
                rabbitTemplate.getConnectionFactory().createConnection().close();

                return RabbitMqStatusDto.builder()
                        .status("CONNECTED")
                        .host(rabbitHost)
                        .port(rabbitPort)
                        .virtualHost(rabbitVirtualHost)
                        .queueName(queueName)
                        .messageCount(0)
                        .consumerCount(0)
                        .exchangeName(exchangeName)
                        .routingKey(routingKey)
                        .message("RabbitMQ bağlantısı sağlandı ancak '" + queueName + "' kuyruğu henüz başlatılmamış.")
                        .build();
            }

        } catch (Exception e) {
            log.warn("RabbitMQ bağlantı ve kuyruk kontrolünde hata: {}", e.getMessage());
            return RabbitMqStatusDto.builder()
                    .status("DISCONNECTED")
                    .host(rabbitHost)
                    .port(rabbitPort)
                    .virtualHost(rabbitVirtualHost)
                    .queueName(queueName)
                    .messageCount(0)
                    .consumerCount(0)
                    .exchangeName(exchangeName)
                    .routingKey(routingKey)
                    .message("RabbitMQ broker bağlantısı kurulamadı: " + (e.getMessage() != null ? e.getMessage() : "Sunucuya ulaşılamıyor"))
                    .build();
        }
    }

    public long clearRedisCache() {
        try {
            Set<String> keys = redisTemplate.keys("short_url:*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                return deleted != null ? deleted : 0L;
            }
            return 0L;
        } catch (Exception e) {
            log.error("Redis önbellek temizleme hatası: {}", e.getMessage());
            throw new RuntimeException("Önbellek temizlenirken hata oluştu: " + e.getMessage());
        }
    }
}
