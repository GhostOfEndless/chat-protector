package ru.tbank.processor.service.personal.payload;

import org.jspecify.annotations.NonNull;
import ru.tbank.common.entity.enums.FilterType;
import ru.tbank.common.telegram.CallbackEvent;
import ru.tbank.processor.excpetion.ButtonNotFoundException;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;

import java.util.NoSuchElementException;

public record CallbackData(
        Integer messageId,
        String callbackId,
        ButtonTextCode pressedButton,
        String[] args
) {
    public static @NonNull CallbackData parseCallbackData(@NonNull CallbackEvent callbackEvent) {
        String[] callbackDataArr = callbackEvent.data().split(":");
        if (!ButtonTextCode.isButton(callbackDataArr[0])) {
            throw new ButtonNotFoundException("Button with name %s not found".formatted(callbackDataArr[0]));
        }
        ButtonTextCode pressedButton = ButtonTextCode.getButtonByName(callbackDataArr[0]);
        String[] args = new String[callbackDataArr.length - 1];
        System.arraycopy(callbackDataArr, 1, args, 0, args.length);
        return new CallbackData(
                callbackEvent.messageId(),
                callbackEvent.id(),
                pressedButton,
                args
        );
    }

    public @NonNull Long getChatId() {
        if (args.length == 0) {
            throw new NoSuchElementException("Callback data doesn't contains chat id");
        }
        return Long.parseLong(args[0]);
    }

    public @NonNull Long getAdminId() {
        if (args.length == 0) {
            throw new NoSuchElementException("Callback data doesn't contains admin id");
        }
        return Long.parseLong(args[0]);
    }

    public @NonNull FilterType getFilterType() {
        if (args.length <= 1 || !FilterType.isFilterType(args[1])) {
            throw new NoSuchElementException("Callback data doesn't contains chat id");
        }
        return FilterType.getFilterType(args[1]);
    }
}
