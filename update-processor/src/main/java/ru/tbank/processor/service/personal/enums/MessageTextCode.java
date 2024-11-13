package ru.tbank.processor.service.personal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import ru.tbank.common.entity.FilterType;

@Getter
@RequiredArgsConstructor
public enum MessageTextCode {
    START_MESSAGE_USER("telegram.start_level.message.user"),
    START_MESSAGE_ADMIN("telegram.start_level.message.admin"),
    START_MESSAGE_OWNER("telegram.start_level.message.owner"),
    CHATS_MESSAGE_OWNER("telegram.chats_level.message.owner"),
    CHATS_MESSAGE_ADMIN("telegram.chats_level.message.admin"),
    CHAT_MESSAGE("telegram.chat_level.message"),
    CHAT_MESSAGE_NOT_FOUND("telegram.chat_level.message.chat_not_found"),
    FILTERS_MESSAGE("telegram.filters_level.message"),
    TEXT_FILTERS_MESSAGE("telegram.text_filters_level.message"),
    TEXT_FILTER_MESSAGE("telegram.text_filter_level.message"),
    CHAT_ADDITION_MESSAGE("telegram.chat_addition_level.message"),
    CHAT_ADDITION_ERROR_MESSAGE("telegram.chat_addition_level.message.error"),
    TEXT_FILTER_TYPE_TAGS("telegram.text_filter_level.filter_type.tags"),
    TEXT_FILTER_TYPE_EMAILS("telegram.text_filter_level.filter_type.emails"),
    TEXT_FILTER_TYPE_LINKS("telegram.text_filter_level.filter_type.links"),
    TEXT_FILTER_TYPE_MENTIONS("telegram.text_filter_level.filter_type.mentions"),
    TEXT_FILTER_TYPE_BOT_COMMANDS("telegram.text_filter_level.filter_type.bot_commands"),
    TEXT_FILTER_TYPE_CUSTOM_EMOJIS("telegram.text_filter_level.filter_type.custom_emojis"),
    TEXT_FILTER_TYPE_PHONE_NUMBERS("telegram.text_filter_level.filter_type.phone_numbers_filter");

    private final String resourceName;

    public static MessageTextCode getFilterNameByType(@NonNull FilterType filterType) {
        return switch (filterType) {
            case TAGS -> TEXT_FILTER_TYPE_TAGS;
            case EMAILS -> TEXT_FILTER_TYPE_EMAILS;
            case LINKS -> TEXT_FILTER_TYPE_LINKS;
            case MENTIONS -> TEXT_FILTER_TYPE_MENTIONS;
            case BOT_COMMANDS -> TEXT_FILTER_TYPE_BOT_COMMANDS;
            case CUSTOM_EMOJIS -> TEXT_FILTER_TYPE_CUSTOM_EMOJIS;
            case PHONE_NUMBERS -> TEXT_FILTER_TYPE_PHONE_NUMBERS;
        };
    }
}
