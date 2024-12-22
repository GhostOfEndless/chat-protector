package ru.tbank.processor.excpetion;

public class ButtonNotFoundException extends ApplicationRuntimeException {

    public ButtonNotFoundException(String message) {
        super(message);
    }
}
