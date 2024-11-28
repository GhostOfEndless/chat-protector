package ru.tbank.processor.service.personal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum Language {
    RUSSIAN("ru"),
    ENGLISH("en");

    private final String languageCode;
    private static final Map<String, Language> codeToLanguage = Map.of(
            "ru", RUSSIAN,
            "en", ENGLISH
    );

    public static Language fromCode(String languageCode) {
        if (!codeToLanguage.containsKey(languageCode)) {
            throw new IllegalArgumentException();
        }

        return codeToLanguage.get(languageCode);
    }
}
