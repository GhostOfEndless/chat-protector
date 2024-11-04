package ru.tbank.processor.service.personal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CallbackTextCode {
    PERMISSION_DENIED("telegram.callback.permission_denied"),
    MESSAGE_EXPIRED("telegram.callback.message_expired"),
    BUTTON_PRESSED("telegram.callback.button_pressed");

    private final String resourceName;
}
