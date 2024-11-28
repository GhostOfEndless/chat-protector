package ru.tbank.processor.service.personal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.tbank.common.entity.FilterType;

import java.util.EnumMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum ButtonTextCode {
    START_BUTTON_CHATS("telegram.start_level.button.chats"),
    START_BUTTON_ADMINS("telegram.start_level.button.admins"),
    START_BUTTON_ACCOUNT("telegram.start_level.button.account"),
    START_BUTTON_LANGUAGE("telegram.start_level.button.language"),
    CHATS_BUTTON_CHAT_ADDITION("telegram.chats_level.button.add_chat"),
    CHAT_ADDITION_BUTTON_ADD("telegram.chat_addition_level.button.add"),
    LANGUAGE_BUTTON_RUSSIAN("telegram.language.russian_language"),
    LANGUAGE_BUTTON_ENGLISH("telegram.language.english_language"),
    ADMINS_BUTTON_ADMIN_ADDITION("telegram.chats_level.button.add_admin"),
    CHAT_BUTTON_FILTERS_SETTINGS("telegram.chat_level.button.filter_settings"),
    FILTERS_BUTTON_TEXT_FILTERS("telegram.filters_level.button.text_filters"),
    TEXT_FILTERS_BUTTON_LINKS("telegram.text_filters_level.button.links_filter"),
    TEXT_FILTERS_BUTTON_TAGS("telegram.text_filters_level.button.tags_filter"),
    TEXT_FILTERS_BUTTON_MENTIONS("telegram.text_filters_level.button.mentions_filter"),
    TEXT_FILTERS_BUTTON_PHONE_NUMBERS("telegram.text_filters_level.button.phone_numbers_filter"),
    TEXT_FILTERS_BUTTON_EMAILS("telegram.text_filters_level.button.emails_filter"),
    TEXT_FILTERS_BUTTON_BOT_COMMANDS("telegram.text_filters_level.button.bot_commands_filter"),
    TEXT_FILTERS_BUTTON_CUSTOM_EMOJIS("telegram.text_filters_level.button.custom_emojis_filter"),
    BUTTON_BACK("telegram.any_level.button_back"),
    TEXT_FILTER_BUTTON_ENABLE("telegram.text_filter_level.button.enable"),
    TEXT_FILTER_BUTTON_DISABLE("telegram.text_filter_level.button.disable"),
    CHAT_BUTTON_EXCLUDE("telegram.chat_level.button.exclude_chat"),
    CHAT_DELETION_BUTTON_CONFIRM("telegram.chat_deletion_level.confirm"),
    CHATS_BUTTON_CHAT(""),
    ADMINS_BUTTON_ADMIN("");

    private final String resourceName;
    private static final EnumMap<ButtonTextCode, FilterType> buttonToFilterType = new EnumMap<>(Map.of(
            TEXT_FILTERS_BUTTON_TAGS, FilterType.TAGS,
            TEXT_FILTERS_BUTTON_MENTIONS, FilterType.MENTIONS,
            TEXT_FILTERS_BUTTON_LINKS, FilterType.LINKS,
            TEXT_FILTERS_BUTTON_PHONE_NUMBERS, FilterType.PHONE_NUMBERS,
            TEXT_FILTERS_BUTTON_EMAILS, FilterType.EMAILS,
            TEXT_FILTERS_BUTTON_BOT_COMMANDS, FilterType.BOT_COMMANDS,
            TEXT_FILTERS_BUTTON_CUSTOM_EMOJIS, FilterType.CUSTOM_EMOJIS
    ));

    private static final EnumMap<ButtonTextCode, Language> buttonToLanguage = new EnumMap<>(Map.of(
            LANGUAGE_BUTTON_RUSSIAN, Language.RUSSIAN,
            LANGUAGE_BUTTON_ENGLISH, Language.ENGLISH
    ));

    private static final EnumMap<Language, ButtonTextCode> languageToButton = new EnumMap<>(Map.of(
            Language.RUSSIAN, LANGUAGE_BUTTON_RUSSIAN,
            Language.ENGLISH, LANGUAGE_BUTTON_ENGLISH
    ));

    public boolean isButtonFilterType() {
        return buttonToFilterType.containsKey(this);
    }

    public FilterType getFilterTypeFromButton() {
        return buttonToFilterType.get(this);
    }

    public boolean isButtonLanguage() {
        return buttonToLanguage.containsKey(this);
    }

    public Language getLanguageFromButton() {
        return buttonToLanguage.get(this);
    }

    public static ButtonTextCode getButtonForLanguage(Language language) {
        return languageToButton.get(language);
    }

    public boolean isBackButton() {
        return this == BUTTON_BACK;
    }

    public boolean isFilterControlButton() {
        return this == TEXT_FILTER_BUTTON_ENABLE || this == TEXT_FILTER_BUTTON_DISABLE;
    }
}
