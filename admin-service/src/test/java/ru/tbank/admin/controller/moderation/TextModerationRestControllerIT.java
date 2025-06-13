package ru.tbank.admin.controller.moderation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import ru.tbank.admin.BaseIT;
import ru.tbank.admin.controller.moderation.payload.TextFilterSettingsRequest;
import ru.tbank.common.entity.ChatModerationSettings;
import ru.tbank.common.entity.enums.FilterMode;

public class TextModerationRestControllerIT extends BaseIT {

    private final static String URI = "/api/v1/admin/settings/chats/{chatId}/text-moderation";

    @Autowired
    private RedisTemplate<String, ChatModerationSettings> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @SneakyThrows
    @Test
    @DisplayName("Should return text moderation settings when config with id was found")
    @WithMockUser(authorities = "ADMIN")
    public void getTextModerationSettingsSuccess() {
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
    @DisplayName("Should return text filter settings when config with id was found")
    @WithMockUser(authorities = "ADMIN")
    public void getTextFilterSettingsSuccess() {
        Long chatId = createTestChat("Chat");
        createConfig(chatId, "Chat");
        mockMvc.perform(get(URI + "/{filterType}", chatId, "links"))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        deleteTestChat(chatId);
    }

    @SneakyThrows
    @DisplayName("Should return text filter settings when config with id was found")
    @WithMockUser(authorities = "ADMIN")
    @ParameterizedTest
    @CsvSource({
            "links,ya.ru",
            "tags,#tag",
            "emails,email@mai.ru",
            "phone-numbers,+79876543210",
            "mentions,@user",
            "bot-commands,/start",
            "custom-emojis,money"
    })
    public void updateChatConfigSuccess(String filterType, String exclusion) {
        Long chatId = createTestChat("Chat");
        createConfig(chatId, "Chat");
        var updatedSettings = TextFilterSettingsRequest.builder()
                .enabled(true)
                .exclusionMode(FilterMode.WHITE_LIST)
                .exclusions(List.of(exclusion))
                .build();
        mockMvc.perform(patch(URI + "/{filterType}", chatId, filterType)
                        .content(objectMapper.writeValueAsString(updatedSettings))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        deleteTestChat(chatId);
    }

    @SneakyThrows
    @Test
    @DisplayName("Should return 400 error due to invalid filter type")
    @WithMockUser(authorities = "ADMIN")
    public void updateChatConfigInvalidFilterType() {
        Long chatId = createTestChat("Chat");
        createConfig(chatId, "Chat");
        var updatedSettings = TextFilterSettingsRequest.builder()
                .enabled(true)
                .exclusionMode(FilterMode.WHITE_LIST)
                .exclusions(Collections.emptyList())
                .build();
        mockMvc.perform(patch(URI + "/{filterType}", chatId, "linkss")
                        .content(objectMapper.writeValueAsString(updatedSettings))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn()
                .getResponse();
        deleteTestChat(chatId);
    }

    @SneakyThrows
    @Test
    @DisplayName("Should return 400error due to invalid exclusion format")
    @WithMockUser(authorities = "ADMIN")
    public void updateChatConfigInvalidExclusionFormat() {
        Long chatId = createTestChat("Chat");
        createConfig(chatId, "Chat");
        var updatedSettings = TextFilterSettingsRequest.builder()
                .enabled(true)
                .exclusionMode(FilterMode.WHITE_LIST)
                .exclusions(List.of("site"))
                .build();
        mockMvc.perform(patch(URI + "/{filterType}", chatId, "links")
                        .content(objectMapper.writeValueAsString(updatedSettings))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
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
