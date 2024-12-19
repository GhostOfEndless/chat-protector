package ru.tbank.receiver.exception;

public class UnknownMessageEntityTypeException extends ApplicationRuntimeException {

    public UnknownMessageEntityTypeException(String message) {
        super(message);
    }
}
