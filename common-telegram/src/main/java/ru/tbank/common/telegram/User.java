package ru.tbank.common.telegram;

public record User(
        Long id,
        String firstName,
        String lastName,
        String userName,
        String languageCode
) {
}
