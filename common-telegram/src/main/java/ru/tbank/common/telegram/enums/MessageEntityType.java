package ru.tbank.common.telegram.enums;

import java.util.Map;

public enum MessageEntityType {
    EMAIL,
    HASHTAG,
    MENTION,
    URL,
    PHONE_NUMBER,
    BOT_COMMAND,
    CUSTOM_EMOJI;

    private static final Map<String, MessageEntityType> nameToType = Map.of(
            "email", EMAIL,
            "hashtag", HASHTAG,
            "mention", MENTION,
            "url", URL,
            "phone_number", PHONE_NUMBER,
            "bot_command", BOT_COMMAND,
            "custom_emoji", CUSTOM_EMOJI
    );

    public static boolean isEntityType(String type) {
        return nameToType.containsKey(type);
    }

    public static MessageEntityType getByType(String type) {
        return nameToType.get(type);
    }
}
