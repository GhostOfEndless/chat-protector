package ru.tbank.processor.service.personal;

public enum UserState {
    START,
    ACCOUNT,
    CHATS,
    CHAT,
    CHAT_ADDITION,
    ADMINS,
    FILTERS,
    ADMIN,
    ADMIN_ADDITION,
    TEXT_FILTERS,
    TEXT_FILTER;

    public boolean matches(String userState) {
        return this.name().equals(userState);
    }
}
