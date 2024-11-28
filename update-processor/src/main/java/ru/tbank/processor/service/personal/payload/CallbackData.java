package ru.tbank.processor.service.personal.payload;

import org.jspecify.annotations.NonNull;
import ru.tbank.common.entity.FilterType;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;

import java.util.NoSuchElementException;

public record CallbackData(
        Integer messageId,
        String callbackId,
        ButtonTextCode pressedButton,
        String[] args
) {

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
