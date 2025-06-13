package ru.tbank.processor.service.moderation;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import ru.tbank.common.entity.spam.SpamProtectionSettings;
import ru.tbank.processor.excpetion.ChatModerationSettingsNotFoundException;

@Service
@NullMarked
@RequiredArgsConstructor
public class SpamProtectionSettingsService {

    private final ChatModerationSettingsService configService;

    public SpamProtectionSettings getSettings(Long chatId) throws ChatModerationSettingsNotFoundException {
        return configService.getChatConfigById(chatId).getSpamProtectionSettings();
    }

    public void updateSettings(Long chatId, boolean newState)
            throws ChatModerationSettingsNotFoundException {
        var chatConfig = configService.getChatConfigById(chatId);
        var spamProtectionSettings = chatConfig.getSpamProtectionSettings();
        spamProtectionSettings.setEnabled(newState);
        configService.updateChatConfig(chatConfig);
    }
}
