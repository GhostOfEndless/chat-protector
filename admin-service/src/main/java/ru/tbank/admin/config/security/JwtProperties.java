package ru.tbank.admin.config.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties("jwt")
public record JwtProperties(
        String secret,
        @DurationUnit(ChronoUnit.SECONDS)
        Duration ttl
) {

    public @NonNull SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
