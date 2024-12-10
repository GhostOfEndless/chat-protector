package ru.tbank.admin.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.admin.auth.payload.AuthenticationRequest;
import ru.tbank.admin.auth.payload.AuthenticationResponse;
import ru.tbank.admin.service.security.AuthenticationService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для аутентификации")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Аутентифицирует пользователя по логину и паролю",
            description = "Возвращает JWT токен при успешной аутентификации по логину и паролю"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class)
                    )),
            @ApiResponse(responseCode = "400", description = "Неверный запрос",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(responseCode = "403", description = "Неверный пароль", content = @Content),
    })
    @PostMapping("/api/v1/auth/authenticate")
    public AuthenticationResponse authenticate(
            @RequestBody
            @Schema(implementation = AuthenticationRequest.class)
            @Valid AuthenticationRequest request
    ) {
        return authenticationService.authenticate(request);
    }
}
