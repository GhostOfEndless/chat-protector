package ru.tbank.admin.exceptions;

import lombok.Getter;

@Getter
public class UserNotFoundException extends ApplicationRuntimeException {

    private final String username;

    public UserNotFoundException(String username) {
        super("username.not_found");
        this.username = username;
    }
}
