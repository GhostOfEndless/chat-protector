package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.tbank.processor.service.personal.handlers.impl.ButtonTextCode;
import ru.tbank.processor.service.personal.handlers.impl.CallbackTextCode;
import ru.tbank.processor.service.personal.handlers.impl.MessageTextCode;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TextResourceService {

    private final MessageSource messageSource;

    public String getMessageText(@NonNull MessageTextCode code, Object[] args, String languageTag) {
        return getTextSource(code.getResourceName(), args, languageTag);
    }

    public String getButtonText(@NonNull ButtonTextCode code, Object[] args, String languageTag) {
        return getTextSource(code.getResourceName(), args, languageTag);
    }

    public String getCallbackText(@NonNull CallbackTextCode code, Object[] args, String languageTag) {
        return getTextSource(code.getResourceName(), args, languageTag);
    }

    public String getMessageText(@NonNull MessageTextCode code, String languageTag) {
        return getTextSource(code.getResourceName(), null, languageTag);
    }

    public String getButtonText(@NonNull ButtonTextCode code, String languageTag) {
        return getTextSource(code.getResourceName(), null, languageTag);
    }

    public String getCallbackText(@NonNull CallbackTextCode code, String languageTag) {
        return getTextSource(code.getResourceName(), null, languageTag);
    }

    private String getTextSource(String resourceCode, Object[] args, String languageTag) {
        return messageSource.getMessage(resourceCode, args, resourceCode, Locale.forLanguageTag(languageTag));
    }
}
