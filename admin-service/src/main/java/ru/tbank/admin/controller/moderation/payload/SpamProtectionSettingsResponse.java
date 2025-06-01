package ru.tbank.admin.controller.moderation.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpamProtectionSettingsResponse(
        @Schema(description = "Состояние защиты", example = "false")
        boolean enabled,
        @Schema(description = "Задержка в секундах перед отправкой следующего сообщения", example = "10")
        long coolDownPeriod,
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
        Set<Long> exclusions
) {
}
