package ru.tbank.processor.service.moderation;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import ru.tbank.common.entity.ChatModerationSettings;
import ru.tbank.processor.excpetion.ChatModerationSettingsNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatModerationSettingsService {

    private static final String UPDATE_CHANNEL_NAME = "configUpdateNotificationChannel";
    private static final String CHAT_PREFIX = "chat:";

    private final RedisTemplate<String, ChatModerationSettings> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final StringRedisTemplate stringRedisTemplate;
    private final Map<Long, ChatModerationSettings> chatConfigs = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        loadAllChatConfigs();

        listenerContainer.addMessageListener(
                (message, pattern) -> {
                    var chatId = new String(message.getBody());
                    updateLocalConfig(chatId);
                },
                new ChannelTopic(UPDATE_CHANNEL_NAME)
        );
    }

    private void loadAllChatConfigs() {
        var keys = redisTemplate.keys(CHAT_PREFIX + "*");

        Objects.requireNonNull(keys).forEach(key ->
                Optional.ofNullable(redisTemplate.opsForValue().get(key))
                        .ifPresent(chatModerationSettings -> {
                            chatConfigs.put(chatModerationSettings.getChatId(), chatModerationSettings);
                            log.info("Loaded chat config for chat id: {}", chatModerationSettings);
                        }));

        log.info("Load '{}' chat configs from DB", keys.size());
    }

    public Optional<ChatModerationSettings> findChatConfigById(Long chatId) {
        return Optional.ofNullable(chatConfigs.get(chatId));
    }

    public ChatModerationSettings getChatConfigById(Long chatId) {
        return findChatConfigById(chatId).orElseThrow(() -> new ChatModerationSettingsNotFoundException(
                "Not found setting for chat with id=%d".formatted(chatId)
        ));
    }

    public void createChatConfig(Long chatId, String chatName) {
        var newConfig = ChatModerationSettings.builder()
                .chatId(chatId)
                .chatName(chatName)
                .build();
        updateChatConfig(newConfig);
        log.info("Default config for chat with id '{}' successfully created!", chatId);
    }

    public void updateChatConfig(@NonNull ChatModerationSettings chatModerationSettings) {
        var key = CHAT_PREFIX + chatModerationSettings.getChatId();
        redisTemplate.opsForValue().set(key, chatModerationSettings);
        stringRedisTemplate.convertAndSend(UPDATE_CHANNEL_NAME, key);
    }

    public void deleteChatConfig(Long chatId) {
        redisTemplate.delete(CHAT_PREFIX + chatId);
    }

    private void updateLocalConfig(String chatId) {
        Optional.ofNullable(redisTemplate.opsForValue().get(chatId))
                .ifPresent(chatModerationSettings -> {
                    log.info("Config updated! {}", chatModerationSettings);
                    chatConfigs.put(chatModerationSettings.getChatId(), chatModerationSettings);
                });
    }
}
