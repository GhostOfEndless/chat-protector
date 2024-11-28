package ru.tbank.processor.service.personal.payload;

import org.jspecify.annotations.NullMarked;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;

@NullMarked
public record CallbackButtonPayload(
        String text,
        String code,
        boolean isUrl
) {
    public static CallbackButtonPayload createChatButton(String chatName, Long chatId) {
        return new CallbackButtonPayload(
                chatName,
                "%s:%d".formatted(ButtonTextCode.CHATS_BUTTON_CHAT, chatId),
                false
        );
    }

    public static CallbackButtonPayload createUserButton(String name, String surname, Long userId) {
        return new CallbackButtonPayload(
                "%s %s".formatted(name, surname),
                "%s:%d".formatted(ButtonTextCode.ADMINS_BUTTON_ADMIN, userId),
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
