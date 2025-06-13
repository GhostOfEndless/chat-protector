package ru.tbank.common.telegram;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
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

    public boolean hasText() {
        return text != null && !text.isBlank();
    }
}
