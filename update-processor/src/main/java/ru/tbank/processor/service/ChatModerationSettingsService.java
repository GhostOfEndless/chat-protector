package ru.tbank.processor.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import ru.tbank.common.entity.ChatModerationSettings;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatModerationSettingsService {

    private final RedisTemplate<String, ChatModerationSettings> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final StringRedisTemplate stringRedisTemplate;
    private final Map<Long, ChatModerationSettings> chatConfigs = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadAllChatConfigs();

        listenerContainer.addMessageListener(
                (message, pattern) -> {
                    var chatId = new String(message.getBody());
                    updateLocalConfig(chatId);
                },
                new ChannelTopic("configUpdateNotificationChannel")
        );
    }

    private void loadAllChatConfigs() {
        var keys = redisTemplate.keys("chat:*");

        Objects.requireNonNull(keys).forEach(key -> {
            var config = redisTemplate.opsForValue().get(key);
            if (!Objects.isNull(config)) {
                chatConfigs.put(config.getChatId(), config);
                log.info("Loaded chat config for chat id: {}", config.getChatId());
            }
        });

        log.info("Load '{}' chat configs from DB", keys.size());
    }

    public ChatModerationSettings getChatConfig(Long chatId) {
        return chatConfigs.computeIfAbsent(chatId, this::fetchFromRedis);
    }

    public void createChatConfig(Long chatId, String chatName) {
        var newConfig = ChatModerationSettings.builder()
                .chatId(chatId)
                .chatName(chatName)
                .build();
        updateChatConfig(newConfig);
        log.info("Default config for chat with id '{}' successfully created!", chatId);
    }

    public void updateChatConfig(ChatModerationSettings chatModerationSettings) {
        var key = "chat:" + chatModerationSettings.getChatId();
        redisTemplate.opsForValue().set(key, chatModerationSettings);
        stringRedisTemplate.convertAndSend("configUpdateNotificationChannel", key);
    }

    private ChatModerationSettings fetchFromRedis(Long chatId) {
        var key = "chat:" + chatId;
        return redisTemplate.opsForValue().get(key);
    }

    private void updateLocalConfig(String chatId) {
        var updatedConfig = Optional.ofNullable(redisTemplate.opsForValue().get(chatId));
        if (updatedConfig.isPresent()) {
            log.info("Config updated! {}", updatedConfig);
            chatConfigs.put(updatedConfig.get().getChatId(), updatedConfig.get());
        }
    }
}
