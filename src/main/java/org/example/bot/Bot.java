package org.example.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.YandexCloudRestClient;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class Bot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final YandexCloudRestClient yandexCloudClient;
    private final String botToken;

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasEntities()) {
            boolean flag = false;
            for (MessageEntity message: update.getMessage().getEntities()) {
                if (message.getType().equals("custom_emoji")) {
                    flag = true;
                }
            }
            if (flag) {
                var deleteMessage = new DeleteMessage(
                        String.valueOf(update.getMessage().getChatId()),
                        update.getMessage().getMessageId());
                try {
                    telegramClient.execute(deleteMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String label = yandexCloudClient.classifyText(messageText);
            log.info("Classification for text \"{}\" is: {}", messageText, label);

            if (!label.equals("обычное сообщение, смех")) {
                var reasonMessage = new SendMessage(
                        String.valueOf(update.getMessage().getChatId()),
                        String.format("_Ваше сообщение\n**>%s||\n было удалено по причине:_\n`%s`", messageText, label));
                reasonMessage.setReplyToMessageId(update.getMessage().getMessageId());
                reasonMessage.enableMarkdownV2(true);


                var deleteMessage = new DeleteMessage(
                        String.valueOf(update.getMessage().getChatId()),
                        update.getMessage().getMessageId());
                try {
                    telegramClient.execute(reasonMessage);
                    telegramClient.execute(deleteMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.info("Registered bot running state is: {}", botSession.isRunning());
    }
}