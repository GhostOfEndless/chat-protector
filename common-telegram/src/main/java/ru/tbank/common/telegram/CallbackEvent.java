package ru.tbank.common.telegram;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CallbackEvent(
        String id,
        Integer messageId,
        String data,
        User user
) implements Serializable {
}
