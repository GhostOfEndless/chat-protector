package ru.tbank.processor.service.group.filter.text;

public enum TextEntityType {
    EMAIL,
    HASHTAG,
    MENTION,
    URL,
    PHONE_NUMBER,
    BOT_COMMAND,
    CUSTOM_EMOJI;

    public boolean isTypeOf(String type) {
        return this.name().toLowerCase().equals(type);
    }
}
