package ru.tbank.common.telegram;

import ru.tbank.common.telegram.enums.ChatType;

public record Chat(
        Long id,
        String title,
        ChatType type
) {
}
