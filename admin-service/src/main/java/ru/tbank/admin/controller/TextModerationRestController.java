package ru.tbank.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.admin.controller.payload.TextFilterSettingsRequest;
import ru.tbank.admin.controller.payload.TextFilterSettingsResponse;
import ru.tbank.admin.controller.payload.TextModerationSettingsResponse;
import ru.tbank.admin.mapper.TextFilterSettingsMapper;
import ru.tbank.admin.mapper.TextModerationSettingsMapper;
import ru.tbank.admin.service.settings.TextModerationSettingsService;
import ru.tbank.common.entity.enums.FilterType;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Текстовая модерация", description = "API настроек текстовой модерации")
public class TextModerationRestController {

    private final TextModerationSettingsService configService;
    private final TextFilterSettingsMapper textFilterSettingsMapper;
    private final TextModerationSettingsMapper textModerationSettingsMapper;

    @Operation(
            summary = "Получение настроек модерации всех текстовых фильтров",
            description = "Возвращает основные настройки текстовых фильтров"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TextModerationSettingsResponse.class)
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
    @GetMapping("/api/v1/admin/settings/chats/{chatId}/text-moderation")
    public TextModerationSettingsResponse getTextModerationSettings(
            @PathVariable("chatId")
            @Parameter(description = "ID Telegram чата", example = "-123456789", required = true)
            Long id
    ) {
        var textModerationSettings = configService.getSettings(id);
        return textModerationSettingsMapper.toTextModerationSettingsResponse(textModerationSettings);
    }

    @Operation(
            summary = "Получение актуальных настроек текстового фильтра",
            description = "Возвращает актуальные настройки текстового фильтра"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TextFilterSettingsResponse.class)
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
    @GetMapping("/api/v1/admin/settings/chats/{chatId}/text-moderation/{filterType}")
    public TextFilterSettingsResponse getTextFilterSettings(
            @PathVariable("chatId")
            @Parameter(description = "ID Telegram чата", example = "-123456789", required = true)
            Long id,
            @PathVariable("filterType")
            @Schema(
                    description = "Тип фильтра",
                    allowableValues = {
                            "emails", "tags", "mentions", "links", "phone-numbers", "bot-commands", "custom-emojis"
                    },
                    example = "links"
            )
            FilterType filterType
    ) {
        var textFilterSettings = configService.getFilterSettings(id, filterType);
        return textFilterSettingsMapper.toTextFilterSettingsResponse(textFilterSettings);
    }

    @Operation(
            summary = "Изменение настроек текстового фильтра",
            description = "Возвращает сохранённые изменения в настройках фильтра"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное сохранение изменений",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TextFilterSettingsResponse.class)
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
    @PatchMapping("/api/v1/admin/settings/chats/{chatId}/text-moderation/{filterType}")
    public TextFilterSettingsResponse updateChatConfig(
            @PathVariable("chatId")
            @Parameter(description = "ID Telegram чата", example = "-123456789", required = true)
            Long id,
            @PathVariable("filterType")
            @Schema(
                    description = "Тип фильтра",
                    allowableValues = {
                            "emails", "tags", "mentions", "links", "phone-numbers", "bot-commands", "custom-emojis"
                    },
                    example = "links"
            )
            FilterType filterType,
            @RequestBody TextFilterSettingsRequest payload
    ) {
        var updatedTextFilterSettings = configService.updateFilterSettings(id, filterType, payload);
        return textFilterSettingsMapper.toTextFilterSettingsResponse(updatedTextFilterSettings);
    }
}
