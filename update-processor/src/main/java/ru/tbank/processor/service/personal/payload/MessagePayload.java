package ru.tbank.processor.service.personal.payload;

import org.jspecify.annotations.NonNull;
import ru.tbank.processor.service.personal.enums.MessageTextCode;

import java.util.Collections;
import java.util.List;

public record MessagePayload(
        MessageTextCode messageText,
        List<MessageArgument> messageArgs,
        List<CallbackButtonPayload> buttons
) {
    public static @NonNull MessagePayload create(MessageTextCode messageText, List<CallbackButtonPayload> buttons) {
        return new MessagePayload(messageText, Collections.emptyList(), buttons);
    }

    public static @NonNull MessagePayload create(
            MessageTextCode messageText,
            List<MessageArgument> messageArgs,
            List<CallbackButtonPayload> buttons
    ) {
        return new MessagePayload(messageText, messageArgs, buttons);
    }
}
