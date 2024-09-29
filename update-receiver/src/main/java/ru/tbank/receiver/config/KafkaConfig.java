package ru.tbank.receiver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Value("${mq.updates-topic-name}")
    private String updates;

    @Bean
    public String updatesTopicName() {
        return updates;
    }
}
