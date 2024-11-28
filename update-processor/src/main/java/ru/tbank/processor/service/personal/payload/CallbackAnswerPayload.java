package ru.tbank.processor.service.personal.payload;

import org.jspecify.annotations.NonNull;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;

import java.util.Collections;
import java.util.List;

public record CallbackAnswerPayload(
        CallbackTextCode callbackText,
        List<CallbackArgument> callbackArgs
) {

    public static @NonNull CallbackAnswerPayload create(CallbackTextCode callbackTextCode) {
        return new CallbackAnswerPayload(callbackTextCode, Collections.emptyList());
    }

    public static @NonNull CallbackAnswerPayload create(
            CallbackTextCode callbackTextCode,
            List<CallbackArgument> callbackArgs
    ) {
        return new CallbackAnswerPayload(callbackTextCode, callbackArgs);
    }
}
