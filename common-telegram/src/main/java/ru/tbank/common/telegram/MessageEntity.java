package ru.tbank.common.telegram;

import lombok.Builder;
import ru.tbank.common.telegram.enums.MessageEntityType;

import java.io.Serializable;

@Builder
public record MessageEntity(
        MessageEntityType type,
        String customEmojiId,
        String text
) implements Serializable {
}
