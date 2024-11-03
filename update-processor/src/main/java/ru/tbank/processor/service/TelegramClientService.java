package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.stickers.GetCustomEmojiStickers;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@NullMarked
@RequiredArgsConstructor
public class TelegramClientService {

    private final TelegramClient telegramClient;

    public List<Sticker> getEmojiPack(String customEmojiId) {
        try {
            var getCustomEmojiStickers = new GetCustomEmojiStickers(List.of(customEmojiId));
            return telegramClient.execute(getCustomEmojiStickers);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public void deleteMessage(Message message) {
        try {
            var deleteMessage = DeleteMessage.builder()
                    .chatId(message.getChatId())
                    .messageId(message.getMessageId())
                    .build();
            telegramClient.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }


    public Message sendMessage(Long chatId, String message, InlineKeyboardMarkup replyMarkup)
            throws TelegramApiException {
        var sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .replyMarkup(replyMarkup)
                .build();
        return telegramClient.execute(sendMessage);
    }
}
