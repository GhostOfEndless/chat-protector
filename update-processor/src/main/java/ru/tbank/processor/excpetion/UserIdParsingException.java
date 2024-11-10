package ru.tbank.processor.excpetion;

public class UserIdParsingException extends ApplicationRuntimeException {

    public UserIdParsingException(String message) {
        super(message);
    }
}
