package ru.tbank.admin.controller.payload;

import ru.tbank.common.entity.FilterMode;

import java.util.List;

public record TextFilterSettingsRequest(
        Boolean enabled,
        FilterMode exclusionMode,
        List<String> exclusions
) {
}
