package ru.tbank.processor.service.personal.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.tbank.common.entity.enums.FilterType;

@Getter
@RequiredArgsConstructor
public enum ButtonTextCode {
    START_CHATS("telegram.start_level.button.chats"),
    START_ADMINS("telegram.start_level.button.admins"),
    START_ACCOUNT("telegram.start_level.button.account"),
    START_LANGUAGE("telegram.start_level.button.language"),
    CHATS_CHAT_ADDITION("telegram.chats_level.button.add_chat"),
    CHAT_ADDITION_ADD("telegram.chat_addition_level.button.add"),
    LANGUAGE_RUSSIAN("telegram.language.russian_language"),
    LANGUAGE_ENGLISH("telegram.language.english_language"),
    ACCOUNT_CHANGE_PASSWORD("telegram.account_level.button.change_password"),
    ADMINS_ADMIN_ADDITION("telegram.chats_level.button.add_admin"),
    ADMIN_REMOVE("telegram.admin_level.button.remove"),
    CHAT_FILTERS_SETTINGS("telegram.chat_level.button.filter_settings"),
    CHAT_SPAM_PROTECTION("telegram.chat_level.button.spam_protection_settings"),
    FILTERS_TEXT_FILTERS("telegram.filters_level.button.text_filters"),
    TEXT_FILTERS_LINKS("telegram.text_filters_level.button.links_filter"),
    TEXT_FILTERS_TAGS("telegram.text_filters_level.button.tags_filter"),
    TEXT_FILTERS_MENTIONS("telegram.text_filters_level.button.mentions_filter"),
    TEXT_FILTERS_PHONE_NUMBERS("telegram.text_filters_level.button.phone_numbers_filter"),
    TEXT_FILTERS_EMAILS("telegram.text_filters_level.button.emails_filter"),
    TEXT_FILTERS_BOT_COMMANDS("telegram.text_filters_level.button.bot_commands_filter"),
    TEXT_FILTERS_CUSTOM_EMOJIS("telegram.text_filters_level.button.custom_emojis_filter"),
    BACK("telegram.any_level.button_back"),
    TEXT_FILTER_ENABLE("telegram.text_filter_level.button.enable"),
    TEXT_FILTER_DISABLE("telegram.text_filter_level.button.disable"),
    SPAM_PROTECTION_ENABLE("telegram.spam_protection_level.button.enable"),
    SPAM_PROTECTION_DISABLE("telegram.spam_protection_level.button.disable"),
    CHAT_EXCLUDE("telegram.chat_level.button.exclude_chat"),
    CHAT_DELETION_CONFIRM("telegram.chat_deletion_level.confirm"),
    CHATS_CHAT(""),
    ADMINS_ADMIN("");

    private static final Map<String, ButtonTextCode> BUTTON_NAMES = Arrays.stream(ButtonTextCode.values())
            .collect(Collectors.toMap(Enum::name, buttonTextCode -> buttonTextCode));
    private static final EnumMap<ButtonTextCode, FilterType> BUTTON_TO_FILTER_TYPE = new EnumMap<>(Map.of(
            TEXT_FILTERS_TAGS, FilterType.TAGS,
            TEXT_FILTERS_MENTIONS, FilterType.MENTIONS,
            TEXT_FILTERS_LINKS, FilterType.LINKS,
            TEXT_FILTERS_PHONE_NUMBERS, FilterType.PHONE_NUMBERS,
            TEXT_FILTERS_EMAILS, FilterType.EMAILS,
            TEXT_FILTERS_BOT_COMMANDS, FilterType.BOT_COMMANDS,
            TEXT_FILTERS_CUSTOM_EMOJIS, FilterType.CUSTOM_EMOJIS
    ));
    private static final EnumMap<ButtonTextCode, Language> BUTTON_TO_LANGUAGE = new EnumMap<>(Map.of(
            LANGUAGE_RUSSIAN, Language.RUSSIAN,
            LANGUAGE_ENGLISH, Language.ENGLISH
    ));
    private static final EnumMap<Language, ButtonTextCode> LANGUAGE_TO_BUTTON = new EnumMap<>(Map.of(
            Language.RUSSIAN, LANGUAGE_RUSSIAN,
            Language.ENGLISH, LANGUAGE_ENGLISH
    ));
    private final String resourceName;

    public static boolean isButton(String buttonName) {
        return BUTTON_NAMES.containsKey(buttonName);
    }

    public static ButtonTextCode getButtonByName(String buttonName) {
        return BUTTON_NAMES.get(buttonName);
    }

    public static ButtonTextCode getButtonForLanguage(Language language) {
        return LANGUAGE_TO_BUTTON.get(language);
    }

    public boolean isButtonFilterType() {
        return BUTTON_TO_FILTER_TYPE.containsKey(this);
    }

    public FilterType getFilterTypeFromButton() {
        return BUTTON_TO_FILTER_TYPE.get(this);
    }

    public boolean isButtonLanguage() {
        return BUTTON_TO_LANGUAGE.containsKey(this);
    }

    public Language getLanguageFromButton() {
        return BUTTON_TO_LANGUAGE.get(this);
    }

    public boolean isBackButton() {
        return this == BACK;
    }

    public boolean isFilterControlButton() {
        return this == TEXT_FILTER_ENABLE || this == TEXT_FILTER_DISABLE;
    }

    public boolean isSpamProtectionControlButton() {
        return this == SPAM_PROTECTION_ENABLE || this == SPAM_PROTECTION_DISABLE;
    }
}
