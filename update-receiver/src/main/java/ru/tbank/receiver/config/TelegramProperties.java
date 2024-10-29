package ru.tbank.receiver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("telegram")
public record TelegramProperties(String token) {
}
