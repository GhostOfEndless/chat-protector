package ru.tbank.common.telegram;

import java.util.List;

public record Message(
        Integer messageId,
        String text,
        User user,
        Chat chat,
        List<MessageEntity> entities
) {
}
