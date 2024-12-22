package ru.tbank.common.telegram;

import java.io.Serializable;
import java.util.List;

public record Message(
        Integer messageId,
        String text,
        User user,
        Chat chat,
        List<MessageEntity> entities
) implements Serializable {
    public boolean hasEntities() {
        return entities != null && !entities.isEmpty();
    }
}
