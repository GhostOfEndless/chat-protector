package ru.tbank.processor.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import ru.tbank.common.entity.ChatConfig;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatConfigService {

    private final RedisTemplate<String, ChatConfig> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final StringRedisTemplate stringRedisTemplate;
    private final Map<Long, ChatConfig> chatConfigs = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        listenerContainer.addMessageListener(
                (message, pattern) -> {
                    var chatId = new String(message.getBody());
                    updateLocalConfig(chatId);
                },
                new ChannelTopic("configUpdateNotificationChannel")
        );
    }

    public ChatConfig getChatConfig(Long chatId) {
        return chatConfigs.computeIfAbsent(chatId, this::fetchFromRedis);
    }

    public void updateChatConfig(ChatConfig chatConfig) {
        var key = "chat:" + chatConfig.getChatId();
        redisTemplate.opsForValue().set(key, chatConfig);
        stringRedisTemplate.convertAndSend("configUpdateNotificationChannel", key);
    }

    private ChatConfig fetchFromRedis(Long chatId) {
        var key = "chat:" + chatId;
        var chatConfig = redisTemplate.opsForValue().get(key);

        if (Objects.isNull(chatConfig)) {
            log.warn("Config for group chat with id '{}' not found. Creating default.", chatId);
            var newConfig = ChatConfig.builder()
                    .chatId(chatId)
                    .build();
            updateChatConfig(newConfig);
            log.info("Default config for chat with id '{}' successfully created!", chatId);
            return newConfig;
        }

        return chatConfig;
    }

    private void updateLocalConfig(String chatId) {
        var updatedConfig = redisTemplate.opsForValue().get(chatId);
        if (!Objects.isNull(updatedConfig)) {
            log.info("Config updated! {}", updatedConfig);
            chatConfigs.put(updatedConfig.getChatId(), updatedConfig);
        }
    }
}
