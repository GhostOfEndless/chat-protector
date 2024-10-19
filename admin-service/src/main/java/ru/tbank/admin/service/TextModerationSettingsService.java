package ru.tbank.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.admin.controller.payload.TextFilterSettingsRequest;
import ru.tbank.admin.exceptions.ChatNotFoundException;
import ru.tbank.common.entity.FilterType;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;

@Service
@RequiredArgsConstructor
public class TextModerationSettingsService {

    private final ChatModerationSettingsService configService;

    public TextModerationSettings getSettings(Long chatId) {
        return configService.findChatConfig(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId))
                .getTextModerationSettings();
    }

    public TextFilterSettings getFilterSettings(Long chatId, FilterType filterType) {
        var textModerationSettings = getSettings(chatId);
        return getFilterSettingsByType(textModerationSettings, filterType);
    }

    public TextFilterSettings updateFilterSettings(Long chatId, FilterType filterType,
                                                   TextFilterSettingsRequest newSettings) {
        var chatModerationSettings = configService.findChatConfig(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));
        var textModerationSettings = chatModerationSettings.getTextModerationSettings();

        // TODO: необходимо добавить валидацию списка исключений в зависимости от типа фильтра
        var filterSettings = getFilterSettingsByType(textModerationSettings, filterType);
        updateFilterSettings(filterSettings, newSettings);
        configService.updateChatConfig(chatModerationSettings);

        // TODO: дождаться обновления конфигурации в Redis.
        //  Если по каким-то причинам обновление не произошло - выбросить ошибку,
        //  иначе вернуть обновлённую конфигурацию

        return filterSettings;
    }

    private TextFilterSettings getFilterSettingsByType(TextModerationSettings settings, FilterType filterType) {
        return switch (filterType) {
            case TAGS -> settings.getTagsFilterSettings();
            case LINKS -> settings.getLinksFilterSettings();
            case EMAILS -> settings.getEmailsFilterSettings();
            case PHONE_NUMBERS -> settings.getPhoneNumbersFilterSettings();
            case MENTIONS -> settings.getMentionsFilterSettings();
        };
    }

    private void updateFilterSettings(TextFilterSettings filterSettings, TextFilterSettingsRequest newSettings) {
        filterSettings.setEnabled(newSettings.enabled());
        filterSettings.setExclusionMode(newSettings.exclusionMode());
        filterSettings.setExclusions(newSettings.exclusions());
    }
}
