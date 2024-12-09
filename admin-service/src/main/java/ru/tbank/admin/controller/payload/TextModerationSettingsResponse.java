package ru.tbank.admin.controller.payload;

public record TextModerationSettingsResponse(
        TextFilterSettingsSimpleResponse linksFilterSettings,
        TextFilterSettingsSimpleResponse tagsFilterSettings,
        TextFilterSettingsSimpleResponse mentionsFilterSettings,
        TextFilterSettingsSimpleResponse phoneNumbersFilterSettings,
        TextFilterSettingsSimpleResponse emailsFilterSettings,
        TextFilterSettingsSimpleResponse botCommandsFilterSettings,
        TextFilterSettingsSimpleResponse customEmojisFilterSettings
) {
}
