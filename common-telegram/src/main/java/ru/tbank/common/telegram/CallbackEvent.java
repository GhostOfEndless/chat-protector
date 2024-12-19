package ru.tbank.common.telegram;

public record CallbackEvent(
        String id,
        Integer messageId,
        String data,
        User user
) {
}
