package ru.tbank.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.tbank.common.entity.text.TextModerationSettings;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatModerationSettings {

    @NotNull
    private Long chatId;
    @NotNull
    private String chatName;

    @NotNull
    @Builder.Default
    private TextModerationSettings textModerationSettings = new TextModerationSettings();
}
