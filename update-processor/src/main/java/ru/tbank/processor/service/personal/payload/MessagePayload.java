package ru.tbank.processor.service.personal.payload;

import lombok.Builder;
import ru.tbank.processor.service.personal.enums.MessageTextCode;

import java.util.List;

@Builder
public record MessagePayload(
        MessageTextCode messageText,
        List<CallbackButtonPayload> buttons,
        String[] messageArgs
) { }
