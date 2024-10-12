package ru.tbank.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.tbank.common.entity.text.TextModerationSettings;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class ChatModerationSettings {

    @NotNull
    private Long chatId;
    @NotNull
    private String chatName;

    @NotNull
    @Builder.Default
    private TextModerationSettings textModerationSettings = new TextModerationSettings();
}
