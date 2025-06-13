package ru.tbank.common.telegram;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record User(
        Long id,
        String firstName,
        String lastName,
        String userName,
        String languageCode
) implements Serializable {
}
