package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.tbank.processor.service.personal.handlers.impl.TextSourceCode;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TextResourceService {

    private final MessageSource messageSource;

    public String getTextSource(@NonNull TextSourceCode code, Object[] args, String languageTag) {
        return messageSource.getMessage(
                code.getResourceName(),
                args,
                code.getResourceName(),
                Locale.forLanguageTag(languageTag)
        );
    }

    public String getTextSource(@NonNull TextSourceCode code, String languageTag) {
        return getTextSource(code, null, languageTag);
    }
}
