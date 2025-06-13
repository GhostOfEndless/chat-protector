package ru.tbank.common.telegram;

import lombok.Builder;
import ru.tbank.common.telegram.enums.ChatType;

import java.io.Serializable;

@Builder
public record Chat(
        Long id,
        String title,
        ChatType type
) implements Serializable {
}
