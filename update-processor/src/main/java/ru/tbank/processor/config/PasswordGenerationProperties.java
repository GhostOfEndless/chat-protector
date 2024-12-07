package ru.tbank.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("password-generator")
public record PasswordGenerationProperties(int passwordLength) {
}
