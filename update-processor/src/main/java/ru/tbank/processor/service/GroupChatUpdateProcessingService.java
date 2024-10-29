package ru.tbank.processor.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.text.TextModerationSettings;
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

    private final TelegramClientService telegramClientService;
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

        processMessage(message, config.getTextModerationSettings());
    }

    private void processMessage(Message message, TextModerationSettings settings) {
        log.debug("Start processing message with id={}", message.getMessageId());

        textFilters.stream()
                .map(filter -> filter.process(message, settings))
                .filter(result -> result != TextProcessingResult.OK)
                .findFirst()
                .ifPresent(result -> {
                    // TODO: сюда нужно добавить сохранение в БД сообщения и причину удаления
                    telegramClientService.deleteMessage(message);
                });

        log.debug("Processed message id={}", message.getMessageId());
    }
}
