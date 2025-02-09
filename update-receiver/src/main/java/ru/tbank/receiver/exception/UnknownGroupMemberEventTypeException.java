package ru.tbank.receiver.exception;

public class UnknownGroupMemberEventTypeException extends ApplicationRuntimeException {

    public UnknownGroupMemberEventTypeException(String message) {
        super(message);
    }
}
