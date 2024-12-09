package ru.tbank.admin.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.tbank.admin.controller.payload.TextFilterSettingsResponse;
import ru.tbank.common.entity.text.TextFilterSettings;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TextFilterSettingsMapper {

    TextFilterSettingsResponse toTextFilterSettingsResponse(TextFilterSettings textFilterSettings);
}
