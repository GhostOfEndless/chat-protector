package ru.tbank.admin.exceptions;

import lombok.Getter;

@Getter
public class UserNotFoundException extends ApplicationRuntimeException {

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("user.id.not_found");
        this.userId = userId;
    }
}
