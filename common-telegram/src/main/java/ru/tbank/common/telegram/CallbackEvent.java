package ru.tbank.common.telegram;

import java.io.Serializable;

public record CallbackEvent(
        String id,
        Integer messageId,
        String data,
        User user
) implements Serializable {
}
