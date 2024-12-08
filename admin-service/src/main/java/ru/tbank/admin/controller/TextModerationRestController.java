package ru.tbank.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.tbank.admin.controller.payload.TextFilterSettingsRequest;
import ru.tbank.admin.service.settings.TextModerationSettingsService;
import ru.tbank.common.entity.enums.FilterType;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;

/**
 * REST контроллер для управления настройками модерации текста в чатах.
 * Предоставляет API для получения и обновления настроек текстовой фильтрации.
 * Все эндпоинты требуют прав администратора.
 */
@RestController
@RequiredArgsConstructor
public class TextModerationRestController {

    private final TextModerationSettingsService configService;

    @GetMapping("/api/v1/admin/settings/chats/{chatId}/text-moderation")
    public TextModerationSettings getTextModerationSettings(@PathVariable("chatId") Long id) {
        return configService.getSettings(id);
    }

    @GetMapping("/api/v1/admin/settings/chats/{chatId}/text-moderation/{filterType}")
    public TextFilterSettings getTextFilterSettings(
            @PathVariable("chatId") Long id,
            @PathVariable("filterType") FilterType filterType
    ) {
        return configService.getFilterSettings(id, filterType);
    }

    @PatchMapping("/api/v1/admin/settings/chats/{chatId}/text-moderation/{filterType}")
    public TextFilterSettings updateChatConfig(
            @PathVariable("chatId") Long id,
            @PathVariable("filterType") FilterType filterType,
            @RequestBody TextFilterSettingsRequest payload
    ) {
        return configService.updateFilterSettings(id, filterType, payload);
    }
}
