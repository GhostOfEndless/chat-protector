package ru.tbank.processor.service.moderation;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import ru.tbank.common.entity.FilterType;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.processor.excpetion.ChatModerationSettingsNotFoundException;

@Service
@NullMarked
@RequiredArgsConstructor
public class TextModerationSettingsService {

    private final ChatModerationSettingsService configService;

    public TextFilterSettings getFilterSettings(Long chatId, FilterType filterType)
            throws ChatModerationSettingsNotFoundException {
        var textModerationSettings = configService.getChatConfigById(chatId).getTextModerationSettings();
        return getFilterSettingsByType(textModerationSettings, filterType);
    }

    public void updateFilterState(Long chatId, FilterType filterType, boolean newState)
            throws ChatModerationSettingsNotFoundException {
        var chatModerationSettings = configService.getChatConfigById(chatId);
        var textModerationSettings = chatModerationSettings.getTextModerationSettings();
        var filterSettings = getFilterSettingsByType(textModerationSettings, filterType);

        filterSettings.setEnabled(newState);
        configService.updateChatConfig(chatModerationSettings);
    }

    private TextFilterSettings getFilterSettingsByType(
            TextModerationSettings textModerationSettings,
            FilterType filterType
    ) {
        return switch (filterType) {
            case TAGS -> textModerationSettings.getTagsFilterSettings();
            case LINKS -> textModerationSettings.getLinksFilterSettings();
            case EMAILS -> textModerationSettings.getEmailsFilterSettings();
            case PHONE_NUMBERS -> textModerationSettings.getPhoneNumbersFilterSettings();
            case MENTIONS -> textModerationSettings.getMentionsFilterSettings();
            case BOT_COMMANDS -> textModerationSettings.getBotCommandsFilterSettings();
            case CUSTOM_EMOJIS -> textModerationSettings.getCustomEmojisFilterSettings();
        };
    }
}
