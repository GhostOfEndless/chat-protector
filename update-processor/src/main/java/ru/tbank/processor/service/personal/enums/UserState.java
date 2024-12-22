package ru.tbank.processor.service.personal.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum UserState {
    ACCOUNT,
    ADMIN,
    ADMIN_ADDITION,
    ADMINS,
    CHANGE_PASSWORD,
    CHAT,
    CHAT_ADDITION,
    CHAT_DELETION,
    CHATS,
    FILTERS,
    LANGUAGE,
    NONE,
    START,
    TEXT_FILTER,
    TEXT_FILTERS;

    @Getter
    private static final List<String> baseNames = Arrays.stream(UserState.values())
            .map(state -> state.name().toLowerCase().replace('_', '-'))
            .toList();
    private static final Map<String, UserState> stateNames = Arrays.stream(UserState.values())
            .collect(Collectors.toMap(Enum::name, userState -> userState));

    public static UserState getUserStateByName(String userStateName) {
        return stateNames.get(userStateName);
    }
}
