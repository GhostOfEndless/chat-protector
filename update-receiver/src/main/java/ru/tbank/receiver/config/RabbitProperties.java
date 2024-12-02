package ru.tbank.receiver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rabbit")
public record RabbitProperties(String updatesTopicName) {
}
