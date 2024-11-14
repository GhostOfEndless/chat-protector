package ru.tbank.processor.excpetion;

public class EntityNotFoundException extends ApplicationRuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
