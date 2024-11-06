package ru.tbank.processor.service.personal.payload;

import ru.tbank.processor.service.personal.enums.UserState;

public record ProcessingResult(
        UserState newState,
        Integer messageId,
        Object[] args
) {
}
