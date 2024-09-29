package ru.tbank.adminpanel.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import ru.tbank.common.entity.ChatConfig;

import java.util.Collection;
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

    public ChatConfig getChatConfig(Long chatId) {
        return chatConfigs.computeIfAbsent(chatId, this::fetchFromRedis);
    }

    public Collection<ChatConfig> getChatConfigs() {
        return chatConfigs.values();
    }

    public void updateChatConfig(ChatConfig chatConfig) {
        var key = "chat:" + chatConfig.getChatId();
        redisTemplate.opsForValue().set(key, chatConfig);
        stringRedisTemplate.convertAndSend("configUpdateNotificationChannel", key);
    }

    private ChatConfig fetchFromRedis(Long chatId) {
        var key = "chat:" + chatId;
        return redisTemplate.opsForValue().get(key);
    }

    private void updateLocalConfig(String chatId) {
        var updatedConfig = redisTemplate.opsForValue().get(chatId);
        if (!Objects.isNull(updatedConfig)) {
            log.info("Config updated! {}", updatedConfig);
            chatConfigs.put(updatedConfig.getChatId(), updatedConfig);
        }
    }
}
