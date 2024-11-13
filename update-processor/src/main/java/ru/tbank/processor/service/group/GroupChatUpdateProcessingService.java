package ru.tbank.processor.service.group;

import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.ChatModerationSettings;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.entity.text.TextProcessingResult;
import ru.tbank.processor.service.ChatModerationSettingsService;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.UpdateProcessingService;
import ru.tbank.processor.service.group.filter.text.TextFilter;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.service.personal.enums.UserRole;
import ru.tbank.processor.utils.enums.UpdateType;

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
    private final AppUserService appUserService;
    private final List<TextFilter> textFilters;

    @PostConstruct
    public void init() {
        Collections.sort(textFilters);
    }

    @Timed("groupMessageProcessing")
    @Override
    public void process(UpdateType updateType, Update update) {
        log.debug("Group update is: {}", update);

        switch (updateType) {
            case GROUP_BOT_ADDED -> {
                long userId = update.getMyChatMember().getFrom().getId();
                var groupChat = update.getMyChatMember().getChat();
                var user = appUserService.findById(userId);

                if (user.isPresent() && user.get().getRole().equals(UserRole.OWNER.name())) {
                    groupChatService.save(groupChat.getId(), groupChat.getTitle());
                    // TODO: заменить на save метод
                    getModerationSettingsByChat(groupChat);
                } else {
                    telegramClientService.leaveFromChat(groupChat.getId());
                }
            }
            case GROUP_BOT_KICKED -> {
                long chatId = update.getMyChatMember().getChat().getId();
                groupChatService.remove(chatId);
                // TODO: удалить конфигурацию чата из Redis
                log.debug("Bot kicked from chat with id: {}", chatId);
            }
            case GROUP_MESSAGE -> {
                var message = update.getMessage();
                var groupChat = groupChatService.findById(message.getChatId());

                if (groupChat.isPresent()) {
                    var chatSettings = getModerationSettingsByChat(message.getChat());
                    processTextMessage(message, chatSettings.getTextModerationSettings());
                }
            }
            default -> log.warn("Unknown update type: {}", update);
        }
    }

    @Timed("groupTextMessageProcessing")
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

    private ChatModerationSettings getModerationSettingsByChat(Chat chat) {
        var config = chatModerationSettingsService.getChatConfig(chat.getId());

        if (Objects.isNull(config)) {
            log.warn("Config is null! Creating the default config...");
            chatModerationSettingsService.createChatConfig(chat.getId(), chat.getTitle());
            config = chatModerationSettingsService.getChatConfig(chat.getId());
        }

        log.debug("Config for this chat: {}", config);

        return config;
    }
}
