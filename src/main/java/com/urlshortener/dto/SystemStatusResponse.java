package com.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sistem Altyapısı Genel Sağlık ve Telemetri Özeti (Redis & RabbitMQ)")
public class SystemStatusResponse {

    @Schema(description = "Sistem genel durumu: HEALTHY, DEGRADED, DOWN", example = "HEALTHY")
    private String overallStatus;

    @Schema(description = "Durum kontrol zaman damgası", example = "2026-08-19T01:55:00Z")
    private String timestamp;

    @Schema(description = "Redis Önbellek Durumu ve Metrikleri")
    private RedisStatusDto redis;

    @Schema(description = "RabbitMQ Mesaj Kuyruğu ve Tıklama İletim Durumu")
    private RabbitMqStatusDto rabbitMq;

    public SystemStatusResponse() {
    }

    public SystemStatusResponse(String overallStatus, String timestamp, RedisStatusDto redis, RabbitMqStatusDto rabbitMq) {
        this.overallStatus = overallStatus;
        this.timestamp = timestamp;
        this.redis = redis;
        this.rabbitMq = rabbitMq;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String overallStatus;
        private String timestamp;
        private RedisStatusDto redis;
        private RabbitMqStatusDto rabbitMq;

        public Builder overallStatus(String overallStatus) {
            this.overallStatus = overallStatus;
            return this;
        }

        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder redis(RedisStatusDto redis) {
            this.redis = redis;
            return this;
        }

        public Builder rabbitMq(RabbitMqStatusDto rabbitMq) {
            this.rabbitMq = rabbitMq;
            return this;
        }

        public SystemStatusResponse build() {
            return new SystemStatusResponse(overallStatus, timestamp, redis, rabbitMq);
        }
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public RedisStatusDto getRedis() {
        return redis;
    }

    public void setRedis(RedisStatusDto redis) {
        this.redis = redis;
    }

    public RabbitMqStatusDto getRabbitMq() {
        return rabbitMq;
    }

    public void setRabbitMq(RabbitMqStatusDto rabbitMq) {
        this.rabbitMq = rabbitMq;
    }
}
