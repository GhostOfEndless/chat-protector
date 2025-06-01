package ru.tbank.admin.controller.moderation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Negative;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.tbank.admin.controller.moderation.payload.*;
import ru.tbank.admin.mapper.SpamProtectionSettingsMapper;
import ru.tbank.admin.service.settings.SpamProtectionSettingsService;

@Validated
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Защита от спама", description = "API настроек защиты от спама")
public class SpamProtectionRestController {

    private final SpamProtectionSettingsService configService;
    private final SpamProtectionSettingsMapper spamProtectionSettingsMapper;

    @Operation(
            summary = "Получение настроек защиты от спама",
            description = "Возвращает текущие настройки защиты от спама"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpamProtectionSettingsResponse.class)
                    )),
            @ApiResponse(responseCode = "400", description = "Неверный запрос",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(responseCode = "401", description = "Срок действия JWT токена истёк",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(responseCode = "403", description = "Пользователь не аутентифицирован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Чат с указанным ID не найден",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping("/api/v1/admin/settings/chats/{chatId}/spam-protection")
    public SpamProtectionSettingsResponse getSpamProtectionSettings(
            @PathVariable("chatId")
            @Parameter(description = "ID Telegram чата", example = "-123456789", required = true)
            @Negative(message = "{chat_id.negative}")
            Long id
    ) {
        var spamProtectionSettings = configService.getSettings(id);
        return spamProtectionSettingsMapper.toSpamProtectionSettingsResponse(spamProtectionSettings);
    }

    @Operation(
            summary = "Изменение настроек защиты от спама",
            description = "Возвращает сохранённые изменения в настройках защиты от спама"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное сохранение изменений",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpamProtectionSettingsResponse.class)
                    )),
            @ApiResponse(responseCode = "400", description = "Неверный запрос",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(responseCode = "401", description = "Срок действия JWT токена истёк",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(responseCode = "403", description = "Пользователь не аутентифицирован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Чат с указанным ID не найден",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PatchMapping("/api/v1/admin/settings/chats/{chatId}/spam-protection")
    public SpamProtectionSettingsResponse updateSpamProtectionSettings(
            @PathVariable("chatId")
            @Parameter(description = "ID Telegram чата", example = "-123456789", required = true)
            @Negative(message = "{chat_id.negative}")
            Long id,
            @RequestBody
            @Schema(implementation = SpamProtectionSettingsRequest.class)
            @Valid SpamProtectionSettingsRequest payload
    ) {
        var updatedSpamProtectionSettings = configService.updateSettings(id, payload);
        return spamProtectionSettingsMapper.toSpamProtectionSettingsResponse(updatedSpamProtectionSettings);
    }
}
