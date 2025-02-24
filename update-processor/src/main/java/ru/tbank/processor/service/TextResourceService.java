package ru.tbank.processor.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;

@Service
@RequiredArgsConstructor
public class TextResourceService {

    private final MessageSource messageSource;

    public String getMessageText(@NonNull MessageTextCode code, Object[] args, String languageTag) {
        return getTextSource(code.getResourceName(), args, languageTag);
    }

    public String getCallbackText(@NonNull CallbackTextCode code, Object[] args, String languageTag) {
        return getTextSource(code.getResourceName(), args, languageTag);
    }

    public String getText(@NonNull String code, String languageTag) {
        return getTextSource(code, null, languageTag);
    }

    private String getTextSource(String resourceCode, Object[] args, String languageTag) {
        return messageSource.getMessage(resourceCode, args, resourceCode, Locale.forLanguageTag(languageTag));
    }
}
