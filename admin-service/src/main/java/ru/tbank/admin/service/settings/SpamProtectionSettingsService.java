package ru.tbank.admin.service.settings;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import ru.tbank.admin.controller.moderation.payload.SpamProtectionSettingsRequest;
import ru.tbank.admin.exceptions.SpamProtectionExclusionValidationException;
import ru.tbank.common.entity.spam.SpamProtectionSettings;

@Service
@RequiredArgsConstructor
public class SpamProtectionSettingsService {

    private final ChatModerationSettingsService configService;

    public SpamProtectionSettings getSettings(Long chatId) {
        return configService.getChatConfig(chatId).getSpamProtectionSettings();
    }

    public SpamProtectionSettings updateSettings(Long chatId, @NonNull SpamProtectionSettingsRequest newSettigns) {
        var chatModerationSettings = configService.getChatConfig(chatId);
        var currentSpamModerationSettings = chatModerationSettings.getSpamProtectionSettings();
        if (!currentSpamModerationSettings.getExclusions().equals(newSettigns.exclusions())) {
            var invalidIdOpt = newSettigns.exclusions().stream().filter(userId -> userId <= 0).findAny();
            if (invalidIdOpt.isPresent()) {
                throw new SpamProtectionExclusionValidationException(invalidIdOpt.get());
            }
        }
        currentSpamModerationSettings.setEnabled(newSettigns.enabled());
        currentSpamModerationSettings.setCoolDownPeriod(newSettigns.coolDownPeriod());
        currentSpamModerationSettings.setExclusions(newSettigns.exclusions());
        configService.updateChatConfig(chatModerationSettings);
        return currentSpamModerationSettings;
    }
}
