package ru.tbank.common.telegram;

import java.io.Serializable;

public record User(
        Long id,
        String firstName,
        String lastName,
        String userName,
        String languageCode
) implements Serializable {
}
