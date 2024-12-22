package ru.tbank.common.telegram;

import ru.tbank.common.telegram.enums.MessageEntityType;

import java.io.Serializable;

public record MessageEntity(
        MessageEntityType type,
        String customEmojiId,
        String text
) implements Serializable {
}
