package ru.tbank.admin.exceptions;

import lombok.Getter;

@Getter
public class UsernameNotFoundException extends ApplicationRuntimeException {

    private final String username;

    public UsernameNotFoundException(String username) {
        super("username.not_found");
        this.username = username;
    }
}
