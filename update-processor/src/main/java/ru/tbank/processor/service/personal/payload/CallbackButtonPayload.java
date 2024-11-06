package ru.tbank.processor.service.personal.payload;

import org.jspecify.annotations.NullMarked;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;

@NullMarked
public record CallbackButtonPayload(
        String text,
        String code
) {
    public static CallbackButtonPayload create(ButtonTextCode textCode) {
        return new CallbackButtonPayload(textCode.getResourceName(), textCode.name());
    }
}
