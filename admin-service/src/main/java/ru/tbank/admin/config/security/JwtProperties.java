package ru.tbank.admin.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties("jwt")
public record JwtProperties(
        String secret,
        @DurationUnit(ChronoUnit.SECONDS)
        Duration ttl
) {
}
