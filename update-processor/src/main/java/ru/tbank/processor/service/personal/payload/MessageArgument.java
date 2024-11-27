package ru.tbank.processor.service.personal.payload;

import org.jspecify.annotations.NonNull;
import ru.tbank.processor.service.personal.enums.LanguageTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;

public record MessageArgument(
        String text,
        boolean isResource
) {

    public static @NonNull MessageArgument createTextArgument(String text) {
        return new MessageArgument(text, false);
    }

    public static @NonNull MessageArgument createResourceArgument(@NonNull MessageTextCode messageTextCode) {
        return new MessageArgument(messageTextCode.getResourceName(), true);
    }

    public static @NonNull MessageArgument createResourceArgument(@NonNull LanguageTextCode languageTextCode) {
        return new MessageArgument(languageTextCode.getLanguageTextCode(), true);
    }
}
