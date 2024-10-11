package ru.tbank.admin.exceptions;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class InvalidFilterTypeException extends RuntimeException {

    public InvalidFilterTypeException() {
        super("settings.filter.invalid_type");
    }
}
