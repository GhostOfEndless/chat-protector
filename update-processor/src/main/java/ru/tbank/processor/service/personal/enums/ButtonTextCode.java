package ru.tbank.processor.service.personal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ButtonTextCode {
    START_BUTTON_CHATS("telegram.start_level.button.chats"),
    START_BUTTON_ADMINS("telegram.start_level.button.admins"),
    START_BUTTON_ACCOUNT("telegram.start_level.button.account");

    private final String resourceName;
}
