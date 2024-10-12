package ru.tbank.common.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FilterType {

    EMAILS("emails"),
    TAGS("tags"),
    MENTIONS("mentions"),
    LINKS("links"),
    PHONE_NUMBERS("phone-numbers");

    private final String type;
}
