package ru.tbank.processor.service.personal.payload;

import org.jspecify.annotations.NullMarked;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;

@NullMarked
public record CallbackButtonPayload(
        String text,
        String code,
        boolean isUrl
) {
    public static CallbackButtonPayload create(String chatName, Long chatId) {
        return new CallbackButtonPayload(
                chatName,
                "%s:%d".formatted(ButtonTextCode.CHATS_BUTTON_CHAT, chatId),
                false
        );
    }

    public static CallbackButtonPayload create(ButtonTextCode textCode) {
        return new CallbackButtonPayload(textCode.getResourceName(), textCode.name(), false);
    }

    public static CallbackButtonPayload create(ButtonTextCode textCode, String url) {
        return new CallbackButtonPayload(textCode.getResourceName(), url, true);
    }

    public static CallbackButtonPayload create(ButtonTextCode textCode, Long chatId) {
        return new CallbackButtonPayload(
                textCode.getResourceName(),
                "%s:%d".formatted(textCode.name(), chatId),
                false);
    }

    public static CallbackButtonPayload create(ButtonTextCode textCode, Long chatId, String additionalData) {
        return new CallbackButtonPayload(
                textCode.getResourceName(),
                "%s:%d:%s".formatted(textCode.name(), chatId, additionalData),
                false
        );
    }
}
