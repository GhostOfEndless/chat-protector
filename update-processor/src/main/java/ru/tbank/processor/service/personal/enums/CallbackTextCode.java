package ru.tbank.processor.service.personal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CallbackTextCode {
    PERMISSION_DENIED("telegram.callback.permission_denied"),
    MESSAGE_EXPIRED("telegram.callback.message_expired"),
    BUTTON_PRESSED("telegram.callback.button_pressed"),
    CHAT_UNAVAILABLE("telegram.callback.chat_unavailable"),
    FILTER_ENABLE("telegram.text_filter.level.callback.filter_enable"),
    FILTER_DISABLE("telegram.text_filter.level.callback.filter_disable"),
    CHAT_REMOVED("telegram.chat_deletion_level.chat_deleted_callback");

    private final String resourceName;
}
