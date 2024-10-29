package ru.tbank.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

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
