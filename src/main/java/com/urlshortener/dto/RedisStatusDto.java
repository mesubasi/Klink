package com.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Redis Önbellek Sunucusu Durum ve Metrik Bilgileri")
public class RedisStatusDto {

    @Schema(description = "Bağlantı durumu: CONNECTED veya DISCONNECTED", example = "CONNECTED")
    private String status;

    @Schema(description = "Redis Host adresi", example = "localhost")
    private String host;

    @Schema(description = "Redis Port numarası", example = "6379")
    private int port;

    @Schema(description = "Ping gecikme süresi (milisaniye)", example = "2")
    private Long pingLatencyMs;

    @Schema(description = "Önbellekte saklanan toplam anahtar sayısı", example = "42")
    private Long totalKeys;

    @Schema(description = "Kullanılan bellek miktarı", example = "1.85MB")
    private String usedMemory;

    @Schema(description = "Redis sürümü", example = "7.2.4")
    private String redisVersion;

    @Schema(description = "Çalışma süresi (gün)", example = "14")
    private Long uptimeDays;

    @Schema(description = "Durum mesajı veya hata açıklaması", example = "Redis bağlantısı aktif ve önbellek hizmet veriyor")
    private String message;

    public RedisStatusDto() {
    }

    public RedisStatusDto(String status, String host, int port, Long pingLatencyMs, Long totalKeys,
                          String usedMemory, String redisVersion, Long uptimeDays, String message) {
        this.status = status;
        this.host = host;
        this.port = port;
        this.pingLatencyMs = pingLatencyMs;
        this.totalKeys = totalKeys;
        this.usedMemory = usedMemory;
        this.redisVersion = redisVersion;
        this.uptimeDays = uptimeDays;
        this.message = message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String status;
        private String host;
        private int port;
        private Long pingLatencyMs;
        private Long totalKeys;
        private String usedMemory;
        private String redisVersion;
        private Long uptimeDays;
        private String message;

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder pingLatencyMs(Long pingLatencyMs) {
            this.pingLatencyMs = pingLatencyMs;
            return this;
        }

        public Builder totalKeys(Long totalKeys) {
            this.totalKeys = totalKeys;
            return this;
        }

        public Builder usedMemory(String usedMemory) {
            this.usedMemory = usedMemory;
            return this;
        }

        public Builder redisVersion(String redisVersion) {
            this.redisVersion = redisVersion;
            return this;
        }

        public Builder uptimeDays(Long uptimeDays) {
            this.uptimeDays = uptimeDays;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public RedisStatusDto build() {
            return new RedisStatusDto(status, host, port, pingLatencyMs, totalKeys, usedMemory, redisVersion, uptimeDays, message);
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Long getPingLatencyMs() {
        return pingLatencyMs;
    }

    public void setPingLatencyMs(Long pingLatencyMs) {
        this.pingLatencyMs = pingLatencyMs;
    }

    public Long getTotalKeys() {
        return totalKeys;
    }

    public void setTotalKeys(Long totalKeys) {
        this.totalKeys = totalKeys;
    }

    public String getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(String usedMemory) {
        this.usedMemory = usedMemory;
    }

    public String getRedisVersion() {
        return redisVersion;
    }

    public void setRedisVersion(String redisVersion) {
        this.redisVersion = redisVersion;
    }

    public Long getUptimeDays() {
        return uptimeDays;
    }

    public void setUptimeDays(Long uptimeDays) {
        this.uptimeDays = uptimeDays;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
