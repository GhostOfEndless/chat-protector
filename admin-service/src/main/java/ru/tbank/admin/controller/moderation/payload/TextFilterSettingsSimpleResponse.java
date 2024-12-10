package ru.tbank.admin.controller.moderation.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.tbank.common.entity.enums.FilterMode;

@Schema(description = "Настройки текстового фильтра")
public record TextFilterSettingsSimpleResponse(
        @Schema(description = "Состояние фильтра", example = "false")
        boolean enabled,
        @Schema(description = "Режим работы фильтра", example = "BLACK_LIST")
        FilterMode exclusionMode
) {
}
