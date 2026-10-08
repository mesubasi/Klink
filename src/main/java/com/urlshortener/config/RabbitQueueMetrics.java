package com.urlshortener.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Exposes the click queue depth and consumer count as Prometheus gauges.
 * Reports NaN when the broker cannot be reached so scrapes never fail.
 */
@Component
public class RabbitQueueMetrics {

    private static final Logger log = LoggerFactory.getLogger(RabbitQueueMetrics.class);

    private final RabbitAdmin rabbitAdmin;
    private final MeterRegistry meterRegistry;

    @Value("${app.rabbitmq.queue:url.click.queue}")
    private String queueName;

    public RabbitQueueMetrics(RabbitAdmin rabbitAdmin, MeterRegistry meterRegistry) {
        this.rabbitAdmin = rabbitAdmin;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    void register() {
        Gauge.builder("klink.rabbitmq.queue.messages", this, m -> m.read(true))
                .description("Messages waiting in the click event queue")
                .tag("queue", queueName)
                .register(meterRegistry);
        Gauge.builder("klink.rabbitmq.queue.consumers", this, m -> m.read(false))
                .description("Active consumers on the click event queue")
                .tag("queue", queueName)
                .register(meterRegistry);
    }

    double read(boolean messages) {
        try {
            QueueInformation info = rabbitAdmin.getQueueInfo(queueName);
            if (info == null) {
                return Double.NaN;
            }
            return messages ? info.getMessageCount() : info.getConsumerCount();
        } catch (Exception e) {
            log.debug("Could not read RabbitMQ queue info: {}", e.getMessage());
            return Double.NaN;
        }
    }
}
