package ru.tbank.processor.service.personal.payload;

import ru.tbank.processor.service.personal.enums.ButtonTextCode;

public record CallbackData(
        ButtonTextCode pressedButton,
        long chatId,
        String additionalData
) {
}
