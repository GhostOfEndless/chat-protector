package ru.tbank.processor.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.tbank.common.entity.ChatModerationSettings;
import ru.tbank.common.entity.text.TextProcessingResult;
import ru.tbank.processor.service.filter.text.TextFilter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@NullMarked
@RequiredArgsConstructor
public class GroupChatUpdateProcessingService {

    private final TelegramClient telegramClient;
    private final ChatModerationSettingsService chatModerationSettingsService;
    private final List<TextFilter> textFilters;

    @PostConstruct
    public void init() {
        Collections.sort(textFilters);
    }

    public void process(Update update) {
        var message = update.getMessage();
        var config = chatModerationSettingsService.getChatConfig(message.getChatId());

        if (Objects.isNull(config)) {
            log.warn("Config is null! Creating the default config...");
            chatModerationSettingsService.createChatConfig(message.getChatId(), message.getChat().getTitle());
            config = chatModerationSettingsService.getChatConfig(message.getChatId());
        }

        log.debug("Config for this chat: {}", config);
        log.debug("Start processing message with id={}", message.getMessageId());

        ChatModerationSettings finalConfig = config;
        textFilters.stream()
                .map(filter -> filter.process(message, finalConfig.getTextModerationSettings()))
                .filter(result -> result != TextProcessingResult.OK)
                .findFirst()
                .ifPresent(result -> {
                    // TODO: сюда нужно добавить сохранение в БД сообщения и причину удаления
                    deleteMessage(message);
                });

        log.debug("Processed message id={}", message.getMessageId());
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
}
