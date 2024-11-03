package ru.tbank.processor.service.personal.handlers.impl;

import lombok.Getter;

@Getter
public enum TextSourceCode {
    START_MESSAGE_ADMIN("telegram.start_level.message.admin"),
    START_MESSAGE_OWNER("telegram.start_level.message.owner"),
    START_BUTTON_CHATS("telegram.start_level.button.chats"),
    START_BUTTON_ADMINS("telegram.start_level.button.admins"),
    START_BUTTON_ACCOUNT("telegram.start_level.button.account");

    private final String resourceName;

    TextSourceCode(String resourceName) {
        this.resourceName = resourceName;
    }
}
