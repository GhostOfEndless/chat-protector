package ru.tbank.processor.service.personal.payload;

import org.jspecify.annotations.NullMarked;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;

@NullMarked
public record CallbackButtonPayload(
        String text,
        String code,
        boolean isUrl
) {
    private static final String BUTTON_DATA_PATTERN = "%s:%d";

    public static CallbackButtonPayload createChatButton(String chatName, Long chatId) {
        return new CallbackButtonPayload(
                chatName,
                BUTTON_DATA_PATTERN.formatted(ButtonTextCode.CHATS_CHAT, chatId),
                false
        );
    }

    public static CallbackButtonPayload createAdminButton(String name, String surname, Long userId) {
        return new CallbackButtonPayload(
                "%s %s".formatted(name, surname),
                BUTTON_DATA_PATTERN.formatted(ButtonTextCode.ADMINS_ADMIN, userId),
                false
        );
    }

    public static CallbackButtonPayload create(ButtonTextCode textCode) {
        return new CallbackButtonPayload(textCode.getResourceName(), textCode.name(), false);
    }

    public static CallbackButtonPayload createUrlButton(ButtonTextCode textCode, String url) {
        return new CallbackButtonPayload(textCode.getResourceName(), url, true);
    }

    public static CallbackButtonPayload create(ButtonTextCode textCode, Long chatId) {
        return new CallbackButtonPayload(
                textCode.getResourceName(),
                BUTTON_DATA_PATTERN.formatted(textCode.name(), chatId),
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
