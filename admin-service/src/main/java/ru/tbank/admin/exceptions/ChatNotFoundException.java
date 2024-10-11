package ru.tbank.admin.exceptions;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ChatNotFoundException extends RuntimeException {

    private final Long chatId;

    public ChatNotFoundException(Long chatId) {
        super("chat.not_found");
        this.chatId = chatId;
    }
}
