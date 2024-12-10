package ru.tbank.admin.controller.moderation.payload;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.tbank.common.entity.enums.FilterMode;

import java.util.List;

public record TextFilterSettingsRequest(
        @Schema(
                description = "Состояние фильтра",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Boolean enabled,
        @Schema(
                description = "Режим работы фильтра",
                example = "BLACK_LIST",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        FilterMode exclusionMode,
        @ArraySchema(
                arraySchema = @Schema(
                        description = "Список исключений",
                        example = """
                                [
                                    "one",
                                    "two"
                                ]
                                """
                )
        )
        List<String> exclusions
) {
}
