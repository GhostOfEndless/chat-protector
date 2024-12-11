package ru.tbank.admin.controller.messages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.admin.controller.messages.payload.DeletedMessageResponse;
import ru.tbank.admin.exceptions.ChatNotFoundException;
import ru.tbank.admin.exceptions.UserNotFoundException;
import ru.tbank.admin.mapper.DeletedMessageMapper;
import ru.tbank.admin.service.persistence.AppUserService;
import ru.tbank.admin.service.persistence.DeletedMessageService;
import ru.tbank.admin.service.persistence.GroupChatService;

@Validated
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Удалённые сообщения", description = "API удалённых сообщений")
public class DeletedMessagesRestController {

    private final DeletedMessageService deletedMessageService;
    private final GroupChatService groupChatService;
    private final AppUserService appUserService;
    private final DeletedMessageMapper deletedMessageMapper;

    @Operation(
            summary = "Постраничное отображение удалённых сообщений",
            description = "Возвращает информацию об удалённых сообщениях в чате"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ", useReturnTypeSchema = true),
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
                    )),
            @ApiResponse(responseCode = "404", description = "Пользователь с указанным ID не найден",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping("/api/v1/admin/chats/{chatId}/deleted-messages")
    public PagedModel<DeletedMessageResponse> getDeletedMessages(
            @PathVariable("chatId")
            @Parameter(description = "ID Telegram чата", example = "-123456789", required = true)
            @Negative(message = "{chat_id.negative}")
            Long chatId,
            @RequestParam(required = false, defaultValue = "0")
            @Parameter(description = "ID пользователя Telegram", example = "123456789")
            @PositiveOrZero(message = "{user_id.positive}")
            Long userId,
            @RequestParam(required = false, defaultValue = "1")
            @Parameter(description = "Номер страницы", example = "1")
            @Min(value = 1, message = "{page.number.min}")
            int page,
            @RequestParam(required = false, defaultValue = "20")
            @Parameter(description = "Размер страницы", example = "20")
            @Min(value = 20, message = "{page.size.min}")
            @Max(value = 100, message = "{page.size.max}")
            int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        if (!groupChatService.existsById(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        if (userId != 0 && !appUserService.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        var deletedMessages = deletedMessageService.getDeletedMessages(chatId, userId, pageable);
        var responses = deletedMessageMapper.toResponsePage(deletedMessages);
        return new PagedModel<>(responses);
    }
}
