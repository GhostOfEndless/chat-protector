package ru.tbank.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.tbank.admin.controller.payload.TextFilterSettingsRequest;
import ru.tbank.admin.service.settings.TextModerationSettingsService;
import ru.tbank.common.entity.FilterType;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;

@RestController
@RequestMapping("/api/v1/settings/chats/{chatId}/text-moderation")
@RequiredArgsConstructor
public class TextModerationRestController {

    private final TextModerationSettingsService configService;

    @GetMapping
    public TextModerationSettings getTextModerationSettings(@PathVariable("chatId") Long id) {
        return configService.getSettings(id);
    }

    @GetMapping("/{filterType}")
    public TextFilterSettings getTextFilterSettings(@PathVariable("chatId") Long id,
                                                    @PathVariable("filterType") FilterType filterType) {
        return configService.getFilterSettings(id, filterType);
    }

    @PatchMapping("/{filterType}")
    public TextFilterSettings updateChatConfig(@PathVariable("chatId") Long id,
                                               @PathVariable("filterType") FilterType filterType,
                                               @RequestBody TextFilterSettingsRequest payload) {
        return configService.updateFilterSettings(id, filterType, payload);
    }
}
