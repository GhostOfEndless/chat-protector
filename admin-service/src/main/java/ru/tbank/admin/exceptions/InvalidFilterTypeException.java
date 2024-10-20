package ru.tbank.admin.exceptions;

public class InvalidFilterTypeException extends ApplicationRuntimeException {

    public InvalidFilterTypeException() {
        super("settings.filter.invalid_type");
    }
}
