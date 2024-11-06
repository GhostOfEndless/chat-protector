package ru.tbank.processor.service.personal.payload;

import ru.tbank.processor.service.personal.enums.MessageTextCode;

import java.util.List;

public record MessagePayload(
        MessageTextCode messageText,
        List<CallbackButtonPayload> buttons
) { }
