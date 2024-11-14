package ru.tbank.processor.excpetion;

public class UnsupportedUpdateType extends ApplicationRuntimeException{

    public UnsupportedUpdateType(String message) {
        super(message);
    }
}
