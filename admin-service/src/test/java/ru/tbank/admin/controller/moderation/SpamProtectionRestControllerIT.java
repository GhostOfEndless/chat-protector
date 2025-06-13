package ru.tbank.admin.controller.moderation;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import ru.tbank.admin.BaseIT;
import ru.tbank.admin.controller.moderation.payload.SpamProtectionSettingsRequest;
import ru.tbank.common.entity.ChatModerationSettings;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SpamProtectionRestControllerIT extends BaseIT {

    private static final String URI = "/api/v1/admin/settings/chats/{chatId}/spam-protection";

    @Autowired
    private RedisTemplate<String, ChatModerationSettings> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @SneakyThrows
    @Test
    @DisplayName("Should return spam protection settings when config with id was found")
    @WithMockUser(authorities = "ADMIN")
    public void getSpamProtectionSettingsSuccess() {
        Long chatId = createTestChat("Chat");
        createConfig(chatId, "Chat");
        mockMvc.perform(get(URI, chatId))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        deleteTestChat(chatId);
    }

    @SneakyThrows
    @Test
    @DisplayName("Should update spam protection settings when config with id was found")
    @WithMockUser(authorities = "ADMIN")
    public void updateChatConfigSuccess() {
        Long chatId = createTestChat("Chat");
        createConfig(chatId, "Chat");
        var updatedSettings = SpamProtectionSettingsRequest.builder()
                .enabled(true)
                .coolDownPeriod(10L)
                .exclusions(Collections.emptySet())
                .build();
        mockMvc.perform(patch(URI, chatId)
                        .content(objectMapper.writeValueAsString(updatedSettings))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        deleteTestChat(chatId);
    }

    public void createConfig(Long chatId, String chatName) {
        var newConfig = ChatModerationSettings.builder()
                .chatId(chatId)
                .chatName(chatName)
                .build();
        String key = "chat:%d".formatted(chatId);
        redisTemplate.opsForValue().set(key, newConfig);
        stringRedisTemplate.convertAndSend("configUpdateNotificationChannel", key);
    }
}
