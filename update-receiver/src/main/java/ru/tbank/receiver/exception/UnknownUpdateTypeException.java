package ru.tbank.receiver.exception;

import org.telegram.telegrambots.meta.api.objects.Update;

public class UnknownUpdateTypeException extends ApplicationRuntimeException {

    public UnknownUpdateTypeException(Update update) {
        super("Can not parse update type for update: %s".formatted(update));
    }
}
