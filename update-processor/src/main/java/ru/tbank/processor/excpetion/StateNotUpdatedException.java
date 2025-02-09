package ru.tbank.processor.excpetion;

public class StateNotUpdatedException extends ApplicationRuntimeException {

    public StateNotUpdatedException(String message) {
        super(message);
    }
}
