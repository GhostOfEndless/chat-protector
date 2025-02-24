package ru.tbank.processor.service.personal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

@Getter
@RequiredArgsConstructor
public enum LanguageTextCode {
    RUSSIAN("telegram.language.russian_language"),
    ENGLISH("telegram.language.english_language");

    private final String languageTextCode;

    public static LanguageTextCode getFromLanguage(@NonNull Language language) {
        return switch (language) {
            case ENGLISH -> LanguageTextCode.ENGLISH;
            case RUSSIAN -> LanguageTextCode.RUSSIAN;
        };
    }
}
