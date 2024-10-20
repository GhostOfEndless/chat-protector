package ru.tbank.admin.exceptions;

import lombok.Getter;

@Getter
public class ChatNotFoundException extends ApplicationRuntimeException {

    private final Long chatId;

    public ChatNotFoundException(Long chatId) {
        super("chat.not_found");
        this.chatId = chatId;
    }
}
