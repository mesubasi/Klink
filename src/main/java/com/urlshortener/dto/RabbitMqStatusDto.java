package com.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "RabbitMQ Mesaj Kuyruğu Durum ve Kuyruk Metrik Bilgileri")
public class RabbitMqStatusDto {

    @Schema(description = "Bağlantı durumu: CONNECTED veya DISCONNECTED", example = "CONNECTED")
    private String status;

    @Schema(description = "RabbitMQ Host adresi", example = "localhost")
    private String host;

    @Schema(description = "RabbitMQ Port numarası", example = "5672")
    private int port;

    @Schema(description = "Virtual Host", example = "/")
    private String virtualHost;

    @Schema(description = "Dinlenen ve izlenen kuyruk adı", example = "url.click.queue")
    private String queueName;

    @Schema(description = "Kuyrukta bekleyen güncel mesaj sayısı", example = "0")
    private Integer messageCount;

    @Schema(description = "Kuyruğu dinleyen aktif tüketici (Consumer) sayısı", example = "1")
    private Integer consumerCount;

    @Schema(description = "Tıklama olaylarının iletildiği Exchange adı", example = "url.click.exchange")
    private String exchangeName;

    @Schema(description = "Routing Key", example = "url.click.routingKey")
    private String routingKey;

    @Schema(description = "Durum veya hata açıklaması", example = "RabbitMQ broker aktif ve kuyruk dinleniyor")
    private String message;

    public RabbitMqStatusDto() {
    }

    public RabbitMqStatusDto(String status, String host, int port, String virtualHost, String queueName,
                             Integer messageCount, Integer consumerCount, String exchangeName,
                             String routingKey, String message) {
        this.status = status;
        this.host = host;
        this.port = port;
        this.virtualHost = virtualHost;
        this.queueName = queueName;
        this.messageCount = messageCount;
        this.consumerCount = consumerCount;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.message = message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String status;
        private String host;
        private int port;
        private String virtualHost;
        private String queueName;
        private Integer messageCount;
        private Integer consumerCount;
        private String exchangeName;
        private String routingKey;
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

        public Builder virtualHost(String virtualHost) {
            this.virtualHost = virtualHost;
            return this;
        }

        public Builder queueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        public Builder messageCount(Integer messageCount) {
            this.messageCount = messageCount;
            return this;
        }

        public Builder consumerCount(Integer consumerCount) {
            this.consumerCount = consumerCount;
            return this;
        }

        public Builder exchangeName(String exchangeName) {
            this.exchangeName = exchangeName;
            return this;
        }

        public Builder routingKey(String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public RabbitMqStatusDto build() {
            return new RabbitMqStatusDto(status, host, port, virtualHost, queueName, messageCount, consumerCount, exchangeName, routingKey, message);
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

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public Integer getConsumerCount() {
        return consumerCount;
    }

    public void setConsumerCount(Integer consumerCount) {
        this.consumerCount = consumerCount;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
