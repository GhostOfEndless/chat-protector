package ru.tbank.processor.service.personal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
    RUSSIAN("ru"),
    ENGLISH("en");

    private final String languageCode;
}
