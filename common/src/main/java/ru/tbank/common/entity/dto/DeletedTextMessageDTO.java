package ru.tbank.common.entity.dto;

import org.jspecify.annotations.NonNull;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.text.TextProcessingResult;

public record DeletedTextMessageDTO(
        Long id,
        Long chatId,
        Integer messageId,
        String messageText,
        Long userId,
        String reason
) {

    public static @NonNull DeletedTextMessageDTO buildDto(@NonNull Message message,
                                                          @NonNull TextProcessingResult result) {
        return new DeletedTextMessageDTO(
                0L,
                message.getChatId(),
                message.getMessageId(),
                message.getText(),
                message.getFrom().getId(),
                result.name()
        );
    }
}
