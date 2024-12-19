package ru.tbank.receiver.exception;

public class UnknownUpdateTypeException extends ApplicationRuntimeException {

    public UnknownUpdateTypeException(String message) {
        super(message);
    }
}
