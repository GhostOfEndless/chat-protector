package ru.tbank.processor.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("redis")
public record RedisProperties(
        int chatConfigsDb,
        int usersStateDb,
        RedisAddress master,
        List<RedisAddress> slaves
) {
    record RedisAddress(
            String host,
            int port
    ) {
    }
}
