package ru.tbank.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rabbit")
public record RabbitProperties(
        String updatesTopicName,
        String exchangeName,
        String groupUpdatesQueueName,
        String groupUpdatesQueueKey,
        String personalUpdatesQueueName,
        String personalUpdatesQueueKey
) {
}
