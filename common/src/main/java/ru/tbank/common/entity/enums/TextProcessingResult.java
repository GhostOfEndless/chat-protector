package ru.tbank.common.entity.enums;

public enum TextProcessingResult {
    OK,
    EMAIL_FOUND,
    TAG_FOUND,
    MENTION_FOUND,
    LINK_FOUND,
    BOT_COMMAND_FOUND,
    CUSTOM_EMOJI_FOUND,
    PHONE_NUMBER_FOUND
}
