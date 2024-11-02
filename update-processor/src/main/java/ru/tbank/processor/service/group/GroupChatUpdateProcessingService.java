package ru.tbank.processor.service.group;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.dto.DeletedTextMessageDTO;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.entity.text.TextProcessingResult;
import ru.tbank.processor.service.UpdateProcessingService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.ChatModerationSettingsService;
import ru.tbank.processor.service.persistence.DeletedTextMessageService;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.group.filter.text.TextFilter;

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
    private final DeletedTextMessageService deletedTextMessageService;
    private final AppUserService appUserService;
    private final GroupChatService groupChatService;
    private final List<TextFilter> textFilters;

    @PostConstruct
    public void init() {
        Collections.sort(textFilters);
    }

    @Override
    public void process(@NonNull Update update) {
        // TODO: логику необходимо поменять так, чтобы конфиг создавался только при добавлении бота
        //  в чат пользователем с ролью owner, иначе бот самостоятельно удаляется из чата
        var message = update.getMessage();
        var config = chatModerationSettingsService.getChatConfig(message.getChatId());

        if (Objects.isNull(config)) {
            log.warn("Config is null! Creating the default config...");
            chatModerationSettingsService.createChatConfig(message.getChatId(), message.getChat().getTitle());
            config = chatModerationSettingsService.getChatConfig(message.getChatId());
            groupChatService.save(message.getChatId(), message.getChat().getTitle());
        }

        log.debug("Config for this chat: {}", config);

        processTextMessage(message, config.getTextModerationSettings());
    }

    private void processTextMessage(Message message, TextModerationSettings settings) {
        log.debug("Start processing message with id={}", message.getMessageId());

        textFilters.stream()
                .map(filter -> filter.process(message, settings))
                .filter(result -> result != TextProcessingResult.OK)
                .findFirst()
                .ifPresent(result -> {
                    telegramClientService.deleteMessage(message);
                    var deletedTextMessage = DeletedTextMessageDTO.buildDto(message, result);
                    var user = message.getFrom();
                    appUserService.saveRegularUser(user.getId(), user.getFirstName(),
                            user.getLastName(), user.getUserName());
                    deletedTextMessageService.save(deletedTextMessage);
                });

        log.debug("Processed message id={}", message.getMessageId());
    }
}
