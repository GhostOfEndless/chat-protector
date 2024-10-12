package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.tbank.common.entity.ChatModerationSettings;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatUpdateProcessingService {

    private final TelegramClient telegramClient;
    private final ChatModerationSettingsService chatModerationSettingsService;

    public void process(Update update) {
        var message = update.getMessage();
        var config = chatModerationSettingsService.getChatConfig(message.getChatId());

        if (Objects.isNull(config)) {
            log.warn("Config is null! Creating ne config");
            chatModerationSettingsService.createChatConfig(message.getChatId(), message.getChat().getTitle());
            config = chatModerationSettingsService.getChatConfig(message.getChatId());
        }

        log.debug("Config for this chat: {}", config);
        processTextMessage(message, config);
    }

    private void deleteMessage(Message message) {
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

    private void processTextMessage(Message message, ChatModerationSettings chatModerationSettings) {
       if (chatModerationSettings.getTextModerationSettings().getTagsFilterSettings().getEnabled() &&
               message.hasEntities() && isMessageContainsBlockedTags(message)) {
            deleteMessage(message);
       }
    }

    private boolean isMessageContainsBlockedTags(Message message) {
        for (MessageEntity entity : message.getEntities()) {
            if (entity.getType().equals("hashtag")) {
                return true;
            }
        }

        return false;
    }
}
