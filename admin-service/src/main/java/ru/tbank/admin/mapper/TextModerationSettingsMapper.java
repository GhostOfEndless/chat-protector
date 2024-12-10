package ru.tbank.admin.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.tbank.admin.controller.moderation.payload.TextModerationSettingsResponse;
import ru.tbank.common.entity.text.TextModerationSettings;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TextModerationSettingsMapper {

    TextModerationSettingsResponse toTextModerationSettingsResponse(TextModerationSettings textModerationSettings);
}
