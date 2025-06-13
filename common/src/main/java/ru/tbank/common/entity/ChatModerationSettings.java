package ru.tbank.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tbank.common.entity.spam.SpamProtectionSettings;
import ru.tbank.common.entity.text.TextModerationSettings;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatModerationSettings {

    private Long chatId;
    private String chatName;

    @Builder.Default
    private TextModerationSettings textModerationSettings = new TextModerationSettings();

    @Builder.Default
    private SpamProtectionSettings spamProtectionSettings = new SpamProtectionSettings();
}
