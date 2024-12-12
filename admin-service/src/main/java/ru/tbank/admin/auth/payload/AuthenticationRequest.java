package ru.tbank.admin.auth.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Данные пользователя для входа")
public record AuthenticationRequest(
        @NotBlank(message = "{login.blank}")
        @Schema(description = "Логин пользователя", example = "user", requiredMode = Schema.RequiredMode.REQUIRED)
        String login,
        @Schema(description = "Пароль пользователя", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{password.blank}")
        String password
) {
}
