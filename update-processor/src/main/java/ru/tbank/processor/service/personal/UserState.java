package ru.tbank.processor.service.personal;

import lombok.Getter;

@Getter
public enum UserState {
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

    private final int allowedRoleLevel;

    UserState(int allowedRoleLevel) {
        this.allowedRoleLevel = allowedRoleLevel;
    }
}
