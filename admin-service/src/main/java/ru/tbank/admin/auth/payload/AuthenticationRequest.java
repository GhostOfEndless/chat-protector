package ru.tbank.admin.auth.payload;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные пользователя для входа")
public record AuthenticationRequest(
        @Schema(description = "Логин пользователя", example = "user", requiredMode = Schema.RequiredMode.REQUIRED)
        String login,
        @Schema(description = "Пароль пользователя", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}
