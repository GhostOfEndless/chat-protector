package ru.tbank.common.entity;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import ru.tbank.common.entity.text.*;

import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ChatModerationSettings implements Serializable {

    @NotNull
    private Long chatId;
    @NotNull
    private String chatName;

    @NotNull
    @Builder.Default
    private TextModerationSettings textModerationSettings = new TextModerationSettings();
}
