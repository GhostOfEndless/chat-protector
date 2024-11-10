package ru.tbank.processor.service.personal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ButtonTextCode {
    START_BUTTON_CHATS("telegram.start_level.button.chats"),
    START_BUTTON_ADMINS("telegram.start_level.button.admins"),
    START_BUTTON_ACCOUNT("telegram.start_level.button.account"),
    CHATS_BUTTON_CHAT_ADDITION("telegram.chats_level.button.add_chat"),
    CHAT_BUTTON_FILTERS_SETTINGS("telegram.chat_level.button.filter_settings"),
    FILTERS_BUTTON_TEXT_FILTERS("telegram.filters_level.button.text_filters"),
    TEXT_FILTERS_BUTTON_LINKS("telegram.text_filters_level.button.links_filter"),
    TEXT_FILTERS_BUTTON_TAGS("telegram.text_filters_level.button.tags_filter"),
    TEXT_FILTERS_BUTTON_MENTIONS("telegram.text_filters_level.button.mentions_filter"),
    TEXT_FILTERS_BUTTON_PHONE_NUMBERS("telegram.text_filters_level.button.phone_numbers_filter"),
    TEXT_FILTERS_BUTTON_EMAILS("telegram.text_filters_level.button.emails_filter"),
    TEXT_FILTERS_BUTTON_BOT_COMMANDS("telegram.text_filters_level.button.bot_commands_filter"),
    TEXT_FILTERS_BUTTON_CUSTOM_EMOJIS("telegram.text_filters_level.button.custom_emojis_filter"),
    BUTTON_BACK("telegram.any_level.button_back");

    private final String resourceName;
}
