package ru.tbank.admin.exceptions;

public class UserNotFoundException extends ApplicationRuntimeException {

    public UserNotFoundException() {
        super("User with specified username not found");
    }
}
