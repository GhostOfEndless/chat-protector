package ru.tbank.common.entity.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
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

    private static final Map<String, FilterType> nameToFilterType = Map.of(
            "EMAILS", EMAILS,
            "TAGS", TAGS,
            "MENTIONS", MENTIONS,
            "LINKS", LINKS,
            "PHONE_NUMBERS", PHONE_NUMBERS,
            "BOT_COMMANDS", BOT_COMMANDS,
            "CUSTOM_EMOJIS", CUSTOM_EMOJIS
    );

    public static boolean isFilterType(String filterType) {
        return nameToFilterType.containsKey(filterType);
    }

    public static FilterType getFilterType(String filterType) {
        return nameToFilterType.get(filterType);
    }
}
