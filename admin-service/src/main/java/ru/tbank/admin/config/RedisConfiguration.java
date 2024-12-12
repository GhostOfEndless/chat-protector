package ru.tbank.admin.config;

import io.lettuce.core.ReadFrom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.tbank.common.entity.ChatModerationSettings;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {

    private final RedisProperties properties;

    @Bean(name = "redisConnectionFactoryConfigDB")
    public LettuceConnectionFactory lettuceConnectionFactoryConfigDB() {
        return createLettuceConnectionFactory(properties.chatConfigsDb());
    }

    @Bean(name = "redisConnectionFactoryUpdateTopic")
    public LettuceConnectionFactory lettuceConnectionFactoryUpdateTopic() {
        log.debug("PUB/SUB: Redis host {} port {}", properties.master().host(), properties.master().port());
        var redisStandaloneConfig = new RedisStandaloneConfiguration(properties.master().host(),
                properties.master().port());

        return new LettuceConnectionFactory(redisStandaloneConfig);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        var container = new RedisMessageListenerContainer();
        container.setConnectionFactory(lettuceConnectionFactoryUpdateTopic());
        return container;
    }

    @Bean
    public RedisTemplate<String, ChatModerationSettings> redisTemplate() {
        var template = new RedisTemplate<String, ChatModerationSettings>();
        template.setConnectionFactory(lettuceConnectionFactoryConfigDB());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatModerationSettings.class));
        template.setKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        return new StringRedisTemplate(lettuceConnectionFactoryUpdateTopic());
    }

    private @NonNull LettuceConnectionFactory createLettuceConnectionFactory(int database) {
        log.debug("Redis master: {} slaves: {} db index: {}", properties.master(), properties.slaves(), database);

        var clientConfig = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build();

        var staticMasterReplicaConfiguration = new RedisStaticMasterReplicaConfiguration(
                properties.master().host(), properties.master().port());
        staticMasterReplicaConfiguration.setDatabase(database);
        properties.slaves().forEach(slave ->
                staticMasterReplicaConfiguration.addNode(slave.host(), slave.port())
        );


        return new LettuceConnectionFactory(staticMasterReplicaConfiguration, clientConfig);
    }
}
