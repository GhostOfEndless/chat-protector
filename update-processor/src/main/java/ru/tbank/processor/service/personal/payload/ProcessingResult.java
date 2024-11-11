package ru.tbank.processor.service.personal.payload;

import org.jspecify.annotations.NonNull;
import ru.tbank.processor.service.personal.enums.UserState;

public record ProcessingResult(
        UserState newState,
        Integer messageId,
        Object[] args
) {

    public static @NonNull ProcessingResult create(UserState newState, Integer messageId) {
        return new ProcessingResult(newState, messageId, new Object[]{});
    }
}
