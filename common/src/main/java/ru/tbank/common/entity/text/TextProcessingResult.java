package ru.tbank.common.entity.text;

public enum TextProcessingResult {
    OK,
    EMAIL_FOUND,
    TAG_FOUND,
    MENTION_FOUND,
    LINK_FOUND,
    PHONE_NUMBER_FOUND
}
