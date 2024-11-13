package ru.tbank.processor.service.personal.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum UserState {
    NONE(UserRole.USER.ordinal()),
    START(UserRole.USER.ordinal()),
    ACCOUNT(UserRole.ADMIN.ordinal()),
    CHATS(UserRole.ADMIN.ordinal()),
    CHAT(UserRole.ADMIN.ordinal()),
    CHAT_ADDITION(UserRole.OWNER.ordinal()),
    ADMINS(UserRole.OWNER.ordinal()),
    FILTERS(UserRole.ADMIN.ordinal()),
    ADMIN(UserRole.OWNER.ordinal()),
    ADMIN_ADDITION(UserRole.OWNER.ordinal()),
    TEXT_FILTERS(UserRole.ADMIN.ordinal()),
    TEXT_FILTER(UserRole.ADMIN.ordinal());

    @Getter
    private static final List<String> baseNames = Arrays.stream(UserState.values())
            .map(state -> state.name().toLowerCase().replace('_', '-'))
            .toList();

    private final int allowedRoleLevel;

    UserState(int allowedRoleLevel) {
        this.allowedRoleLevel = allowedRoleLevel;
    }
}
