package ru.tbank.processor.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitConfiguration {

    private final RabbitProperties rabbitProperties;

    @Bean
    public SimpleMessageConverter converter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.setAllowedListPatterns(List.of("java.*", "ru.tbank.common.telegram.*"));
        return converter;
    }

    @Bean
    public Queue groupUpdatesQueue() {
        return new Queue(rabbitProperties.groupUpdatesQueueName());
    }

    @Bean
    public Queue personalUpdatesQueue() {
        return new Queue(rabbitProperties.personalUpdatesQueueName());
    }
}
