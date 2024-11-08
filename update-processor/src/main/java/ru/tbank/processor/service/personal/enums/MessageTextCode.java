package ru.tbank.processor.service.personal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageTextCode {
    START_MESSAGE_USER("telegram.start_level.message.user"),
    START_MESSAGE_ADMIN("telegram.start_level.message.admin"),
    START_MESSAGE_OWNER("telegram.start_level.message.owner"),
    CHATS_MESSAGE_OWNER("telegram.chats_level.message.owner"),
    CHATS_MESSAGE_ADMIN("telegram.chats_level.message.admin"),
    CHAT_MESSAGE("telegram.chat_level.message"),
    CHAT_MESSAGE_NOT_FOUND("telegram.chat_level.message.chat_not_found"),
    FILTERS_MESSAGE("telegram.filters_level.message"),
    TEXT_FILTERS_MESSAGE("telegram.text_filters_level.message"),;

    private final String resourceName;
}
