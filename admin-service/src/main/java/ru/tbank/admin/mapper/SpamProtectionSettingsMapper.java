package ru.tbank.admin.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.tbank.admin.controller.moderation.payload.SpamProtectionSettingsResponse;
import ru.tbank.common.entity.spam.SpamProtectionSettings;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SpamProtectionSettingsMapper {

    SpamProtectionSettingsResponse toSpamProtectionSettingsResponse(SpamProtectionSettings settings);
}
