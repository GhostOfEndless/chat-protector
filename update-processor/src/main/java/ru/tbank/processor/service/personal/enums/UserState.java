package ru.tbank.processor.service.personal.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum UserState {
    NONE,
    START,
    ACCOUNT,
    CHATS,
    LANGUAGE,
    CHAT,
    CHAT_ADDITION,
    ADMINS,
    FILTERS,
    ADMIN,
    ADMIN_ADDITION,
    TEXT_FILTERS,
    TEXT_FILTER,
    CHAT_DELETION;

    @Getter
    private static final List<String> baseNames = Arrays.stream(UserState.values())
            .map(state -> state.name().toLowerCase().replace('_', '-'))
            .toList();
}
