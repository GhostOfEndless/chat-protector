package ru.tbank.processor.service.personal.enums;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
    RUSSIAN("ru"),
    ENGLISH("en");

    private static final Map<String, Language> CODE_TO_LANGUAGE = Map.of(
            "ru", RUSSIAN,
            "en", ENGLISH
    );
    private final String languageCode;

    public static Language fromCode(String languageCode) {
        if (!CODE_TO_LANGUAGE.containsKey(languageCode)) {
            throw new IllegalArgumentException();
        }

        return CODE_TO_LANGUAGE.get(languageCode);
    }
}
