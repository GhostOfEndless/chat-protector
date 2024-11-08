package ru.tbank.processor.service.personal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ButtonTextCode {
    START_BUTTON_CHATS("telegram.start_level.button.chats"),
    START_BUTTON_ADMINS("telegram.start_level.button.admins"),
    START_BUTTON_ACCOUNT("telegram.start_level.button.account"),
    CHATS_BUTTON_CHAT_ADDITION("telegram.chats_level.button.add_chat"),
    CHAT_BUTTON_FILTERS_SETTINGS("telegram.chat_level.button.filter_settings"),
    FILTERS_BUTTON_TEXT_FILTERS("telegram.filters_level.button.text_filters"),
    BUTTON_BACK("telegram.any_level.button_back");

    private final String resourceName;
}
