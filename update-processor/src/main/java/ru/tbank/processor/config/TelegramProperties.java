package ru.tbank.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("telegram")
public record TelegramProperties(
        String token,
        String botAdditionUrl,
        Long ownerId,
        String schema,
        String host,
        Integer port
) {
}
