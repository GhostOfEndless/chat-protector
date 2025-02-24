package ru.tbank.admin.controller.users.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record UserResponse(
        @Schema(description = "ID пользователя в Telegram", example = "123456789")
        Long id,
        @Schema(description = "Имя пользователя в Telegram", example = "Ivan")
        String firstName,
        @Schema(description = "Фамилия пользователя в Telegram", example = "Ivanov")
        String lastName,
        @Schema(description = "Username пользователя в Telegram", example = "username")
        String username,
        @Schema(
                description = "Дата и время первого взаимодействия пользователя с системой",
                example = "2024-12-09 10:15:47"
        )
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime additionDate
) {
}
