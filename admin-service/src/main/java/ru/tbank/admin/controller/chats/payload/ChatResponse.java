package ru.tbank.admin.controller.chats.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ChatResponse(
        @Schema(description = "ID Telegram чата", example = "-123456789")
        Long id,
        @Schema(description = "Название чата", example = "Title")
        String name,
        @Schema(description = "Дата и время добавления чата в систему", example = "2024-12-09 10:15:47")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime additionDate
) {
}
