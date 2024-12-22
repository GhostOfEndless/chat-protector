package ru.tbank.receiver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitConfiguration {

    private final RabbitProperties rabbitProperties;

    @Bean
    public Queue groupUpdatesQueue() {
        return new Queue(rabbitProperties.groupUpdatesQueueName());
    }

    @Bean
    public Queue personalUpdatesQueue() {
        return new Queue(rabbitProperties.personalUpdatesQueueName());
    }

    @Bean
    public Exchange updatesExchange() {
        return new TopicExchange(rabbitProperties.exchangeName());
    }

    @Bean
    public Binding groupUpdatesBinding(Queue groupUpdatesQueue, Exchange updatesExchange) {
        return BindingBuilder.bind(groupUpdatesQueue)
                .to(updatesExchange)
                .with(rabbitProperties.groupUpdatesQueueKey())
                .noargs();
    }

    @Bean
    public Binding personalUpdatesBinding(Queue personalUpdatesQueue, Exchange updatesExchange) {
        return BindingBuilder.bind(personalUpdatesQueue)
                .to(updatesExchange)
                .with(rabbitProperties.personalUpdatesQueueKey())
                .noargs();
    }
}
