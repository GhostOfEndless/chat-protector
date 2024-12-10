package ru.tbank.admin.controller.moderation.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.tbank.common.entity.enums.FilterMode;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TextFilterSettingsResponse(
        @Schema(description = "Состояние фильтра", example = "false")
        boolean enabled,
        @Schema(description = "Режим работы фильтра", example = "BLACK_LIST")
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
