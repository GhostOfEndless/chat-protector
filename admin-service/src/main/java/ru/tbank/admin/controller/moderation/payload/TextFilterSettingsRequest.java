package ru.tbank.admin.controller.moderation.payload;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import ru.tbank.common.entity.enums.FilterMode;

@Builder
public record TextFilterSettingsRequest(
        @Schema(
                description = "Состояние фильтра",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "{filter.state.is_null}")
        Boolean enabled,
        @Schema(
                description = "Режим работы фильтра",
                example = "BLACK_LIST",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "{filter.mode.is_null}")
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
        @NotNull(message = "{filter.exclusions.is_null}")
        List<String> exclusions
) {
}
