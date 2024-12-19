package ru.tbank.receiver.exception;

public class UnknownChatTypeException extends ApplicationRuntimeException {

    public UnknownChatTypeException(String message) {
        super(message);
    }
}
