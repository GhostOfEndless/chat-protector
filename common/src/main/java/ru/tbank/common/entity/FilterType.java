package ru.tbank.common.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum FilterType {

    EMAILS,
    TAGS,
    MENTIONS,
    LINKS,
    PHONE_NUMBERS,
    BOT_COMMANDS,
    CUSTOM_EMOJIS;

    @Getter
    private static final String availableTypes = Arrays.stream(values())
            .map(filterType -> filterType.name()
                    .toLowerCase()
                    .replace('_', '-'))
            .collect(Collectors.joining(", "));

}
