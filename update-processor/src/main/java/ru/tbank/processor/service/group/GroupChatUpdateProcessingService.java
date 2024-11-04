package ru.tbank.processor.service.group;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.ChatModerationSettings;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.entity.text.TextProcessingResult;
import ru.tbank.processor.service.ChatModerationSettingsService;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.UpdateProcessingService;
import ru.tbank.processor.service.group.filter.text.TextFilter;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.utils.UpdateType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@NullMarked
@RequiredArgsConstructor
public class GroupChatUpdateProcessingService implements UpdateProcessingService {

    private final TelegramClientService telegramClientService;
    private final ChatModerationSettingsService chatModerationSettingsService;
    private final DeletedMessageReportService reportService;
    private final GroupChatService groupChatService;
    private final List<TextFilter> textFilters;

    @PostConstruct
    public void init() {
        Collections.sort(textFilters);
    }

    @Override
    public void process(UpdateType updateType, Update update) {
        // TODO: логику необходимо поменять так, чтобы конфиг создавался только при добавлении бота
        //  в чат пользователем с ролью owner, иначе бот самостоятельно удаляется из чата
        var message = update.getMessage();
        var chatSettings = getSettingsForChatByMessage(message);
        groupChatService.findById(message.getChatId())
                .orElseGet(() -> groupChatService.save(message.getChatId(), message.getChat().getTitle()));

        processTextMessage(message, chatSettings.getTextModerationSettings());
    }

    private void processTextMessage(Message message, TextModerationSettings settings) {
        log.debug("Start processing message with id={}", message.getMessageId());

        textFilters.stream()
                .map(filter -> filter.process(message, settings))
                .filter(result -> result != TextProcessingResult.OK)
                .findFirst()
                .ifPresent(result -> {
                    telegramClientService.deleteMessage(message.getChatId(), message.getMessageId());
                    reportService.saveReport(message, result);
                });

        log.debug("Processed message id={}", message.getMessageId());
    }

    private ChatModerationSettings getSettingsForChatByMessage(Message message) {
        var config = chatModerationSettingsService.getChatConfig(message.getChatId());

        if (Objects.isNull(config)) {
            log.warn("Config is null! Creating the default config...");
            chatModerationSettingsService.createChatConfig(message.getChatId(), message.getChat().getTitle());
            config = chatModerationSettingsService.getChatConfig(message.getChatId());
            groupChatService.save(message.getChatId(), message.getChat().getTitle());
        }

        log.debug("Config for this chat: {}", config);

        return config;
    }
}
