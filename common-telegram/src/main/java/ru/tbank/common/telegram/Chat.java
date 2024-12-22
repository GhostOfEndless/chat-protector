package ru.tbank.common.telegram;

import ru.tbank.common.telegram.enums.ChatType;

import java.io.Serializable;

public record Chat(
        Long id,
        String title,
        ChatType type
) implements Serializable {
}
