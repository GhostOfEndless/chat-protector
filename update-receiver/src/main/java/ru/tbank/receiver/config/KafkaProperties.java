package ru.tbank.receiver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kafka")
public record KafkaProperties(String updatesTopic) {
}
