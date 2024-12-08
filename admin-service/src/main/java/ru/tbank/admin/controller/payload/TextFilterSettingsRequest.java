package ru.tbank.admin.controller.payload;

import ru.tbank.common.entity.enums.FilterMode;

import java.util.List;

public record TextFilterSettingsRequest(
        Boolean enabled,
        FilterMode exclusionMode,
        List<String> exclusions
) {
}
