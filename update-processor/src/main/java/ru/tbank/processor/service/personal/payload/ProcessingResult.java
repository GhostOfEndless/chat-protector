package ru.tbank.processor.service.personal.payload;

import org.jspecify.annotations.NonNull;
import ru.tbank.processor.service.personal.enums.UserState;

public record ProcessingResult(
        UserState newState,
        Integer messageId,
        Object[] args
) {

    public static @NonNull ProcessingResult create(UserState newState, Integer messageId, Object... args) {
        return new ProcessingResult(newState, messageId, args);
    }

    public static @NonNull ProcessingResult create(UserState newState, Integer messageId) {
        return ProcessingResult.create(newState, messageId, new Object[0]);
    }

    public static @NonNull ProcessingResult create(UserState newState, Object... args) {
        return ProcessingResult.create(newState, 0, args);
    }

    public static @NonNull ProcessingResult create(UserState newState) {
        return ProcessingResult.create(newState, 0);
    }
}
