package ru.tbank.admin.controller.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.admin.controller.users.payload.UserResponse;
import ru.tbank.admin.mapper.UserMapper;
import ru.tbank.admin.service.persistence.AppUserService;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Пользователи", description = "API пользователей")
public class UsersRestController {

    private final AppUserService appUserService;
    private final UserMapper userMapper;

    @Operation(
            summary = "Информация о пользователе",
            description = "Возвращает информацию о пользователе по его id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
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
            @ApiResponse(responseCode = "404", description = "Пользователь с указанным ID не найден",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping("/api/v1/admin/users/{userId}")
    public UserResponse getById(
            @PathVariable("userId")
            @Parameter(description = "ID пользователя Telegram", example = "123456789", required = true)
            Long userId
    ) {
        var user = appUserService.getById(userId);
        return userMapper.toUserResponse(user);
    }

    @Operation(
            summary = "Постраничное отображение пользователей",
            description = "Возвращает информацию о всех пользователях, когда-либо взаимодействовавших с системой"
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
            @ApiResponse(responseCode = "403", description = "Пользователь не аутентифицирован", content = @Content)
    })
    @GetMapping("/api/v1/admin/users")
    public PagedModel<UserResponse> findAll(
            @RequestParam(required = false, defaultValue = "1")
            @Parameter(description = "Номер страницы", example = "1")
            int page,
            @RequestParam(required = false, defaultValue = "20")
            @Parameter(description = "Размер страницы", example = "20")
            int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        var applicationUsers = appUserService.getApplicationUsers(pageable);
        var responses = userMapper.toResponsePage(applicationUsers);
        return new PagedModel<>(responses);
    }
}
