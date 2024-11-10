package ru.tbank.processor.service.personal.payload;

import ru.tbank.processor.service.personal.enums.ButtonTextCode;

public record ChatIdCallbackData(
        ButtonTextCode pressedButton,
        long chatId
) {
}
