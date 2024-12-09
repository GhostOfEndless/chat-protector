package ru.tbank.admin.controller.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record DeletedMessageResponse(
        @Schema(description = "ID удаленного сообщения в БД", example = "1000")
        Long id,
        @Schema(description = "ID Telegram чата", example = "-123456789")
        Long chatId,
        @Schema(description = "ID удаленного сообщения в Telegram чате", example = "56")
        Integer messageId,
        @Schema(description = "Текст удалённого сообщения", example = "ya.ru")
        String messageText,
        @Schema(description = "ID Telegram пользователя", example = "123456789")
        Long userId,
        @Schema(description = "Дата и время добавления чата в систему", example = "2024-12-09 10:15:47")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime deletionTime,
        @Schema(description = "Причина удаления сообщения", example = "LINK")
        String reason
) {
}
