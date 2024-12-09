package ru.tbank.admin.service.settings;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import ru.tbank.admin.controller.payload.TextFilterSettingsRequest;
import ru.tbank.common.entity.enums.FilterType;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;

@Service
@RequiredArgsConstructor
public class TextModerationSettingsService {

    private final ChatModerationSettingsService configService;
    private final TextFilterSettingsExcludesValidator validator;

    public TextModerationSettings getSettings(Long chatId) {
        return configService.getChatConfig(chatId).getTextModerationSettings();
    }

    public TextFilterSettings getFilterSettings(Long chatId, FilterType filterType) {
        var textModerationSettings = configService.getChatConfig(chatId).getTextModerationSettings();
        return getFilterSettingsByType(textModerationSettings, filterType);
    }

    public TextFilterSettings updateFilterSettings(
            Long chatId,
            FilterType filterType,
            @NonNull TextFilterSettingsRequest newSettings
    ) {
        var chatModerationSettings = configService.getChatConfig(chatId);
        var textModerationSettings = chatModerationSettings.getTextModerationSettings();
        var currentTextFilterSettings = getFilterSettingsByType(textModerationSettings, filterType);
        if (!currentTextFilterSettings.getExclusions().equals(newSettings.exclusions())) {
            validator.validate(filterType, newSettings);
        }
        var filterSettings = getFilterSettingsByType(textModerationSettings, filterType);
        updateFilterSettings(filterSettings, newSettings);
        configService.updateChatConfig(chatModerationSettings);
        // TODO: дождаться обновления конфигурации в Redis.
        //  Если по каким-то причинам обновление не произошло - выбросить ошибку,
        //  иначе вернуть обновлённую конфигурацию
        return filterSettings;
    }

    private TextFilterSettings getFilterSettingsByType(
            TextModerationSettings settings,
            @NonNull FilterType filterType
    ) {
        return switch (filterType) {
            case TAGS -> settings.getTagsFilterSettings();
            case LINKS -> settings.getLinksFilterSettings();
            case EMAILS -> settings.getEmailsFilterSettings();
            case PHONE_NUMBERS -> settings.getPhoneNumbersFilterSettings();
            case MENTIONS -> settings.getMentionsFilterSettings();
            case BOT_COMMANDS -> settings.getBotCommandsFilterSettings();
            case CUSTOM_EMOJIS -> settings.getCustomEmojisFilterSettings();
        };
    }

    private void updateFilterSettings(
            @NonNull TextFilterSettings filterSettings,
            @NonNull TextFilterSettingsRequest newSettings
    ) {
        filterSettings.setEnabled(newSettings.enabled());
        filterSettings.setExclusionMode(newSettings.exclusionMode());
        filterSettings.setExclusions(newSettings.exclusions());
    }
}
