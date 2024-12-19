package ru.tbank.common.telegram;

import ru.tbank.common.telegram.enums.MessageEntityType;

public record MessageEntity(
        MessageEntityType entityType,
        String customEmojiId,
        String text
) {
}
