package ru.tbank.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.tbank.common.entity.ChatModerationSettings;

import java.nio.charset.StandardCharsets;

@Configuration
public class RedisConfiguration {

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, ChatModerationSettings> redisTemplate() {
        var template = new RedisTemplate<String, ChatModerationSettings>();
        template.setConnectionFactory(lettuceConnectionFactory());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatModerationSettings.class));
        template.setKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        var container = new RedisMessageListenerContainer();
        container.setConnectionFactory(lettuceConnectionFactory());
        return container;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        return new StringRedisTemplate(lettuceConnectionFactory());
    }
}
