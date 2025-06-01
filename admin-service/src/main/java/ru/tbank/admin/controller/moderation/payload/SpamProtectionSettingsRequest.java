package ru.tbank.admin.controller.moderation.payload;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Set;

@Builder
public record SpamProtectionSettingsRequest(
        @Schema(
                description = "Состояние защиты",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "{protection.state.is_null}")
        Boolean enabled,
        @Schema(
                description = "Задержка в секундах перед отправкой следующего сообщения",
                example = "10",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "{protection.cool-down-period.is_null}")
        Long coolDownPeriod,
        @ArraySchema(
                arraySchema = @Schema(
                        description = "Список id пользователей, на которых не распространяется медленный режим",
                        example = """
                                [
                                    123456789,
                                    987654321
                                ]
                                """
                )
        )
        @NotNull(message = "{protection.exclusions.is_null}")
        Set<Long> exclusions

) {
}
