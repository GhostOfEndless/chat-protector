package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.stickers.GetCustomEmojiStickers;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.User;
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

    private static final String MESSAGE_TEXT_PARSE_MODE = "MarkdownV2";
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

    public void deleteMessage(Long chatId, Integer messageId) {
        try {
            var deleteMessage = DeleteMessage.builder()
                    .chatId(chatId)
                    .messageId(messageId)
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
                .parseMode(MESSAGE_TEXT_PARSE_MODE)
                .build();
        return telegramClient.execute(sendMessage);
    }

    public void editMessage(Long chatId, Integer messageId, String message, InlineKeyboardMarkup replyMarkup)
            throws TelegramApiException {
        var editMessage = EditMessageText.builder()
                .messageId(messageId)
                .replyMarkup(replyMarkup)
                .chatId(chatId)
                .text(message)
                .parseMode(MESSAGE_TEXT_PARSE_MODE)
                .build();
        telegramClient.execute(editMessage);
    }

    public void sendCallbackAnswer(String text, String callbackQueryId, boolean isAlert) {
        try {
            var callbackAnswer = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .showAlert(isAlert)
                    .text(text)
                    .build();
            telegramClient.execute(callbackAnswer);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public User getMe() {
        try {
            return telegramClient.execute(new GetMe());
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            return new User(0L, "", true);
        }
    }

    public void leaveFromChat(Long chatId) {
        try {
            telegramClient.execute(new LeaveChat(chatId.toString()));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
