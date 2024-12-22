package ru.tbank.processor.service.group;

import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.tbank.common.entity.enums.TextProcessingResult;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.telegram.Chat;
import ru.tbank.common.telegram.GroupMemberEvent;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.TelegramUpdate;
import ru.tbank.common.telegram.User;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.group.filter.text.TextFilter;
import ru.tbank.processor.service.moderation.ChatModerationSettingsService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.GroupChatService;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@NullMarked
@RequiredArgsConstructor
public class GroupChatUpdateProcessingService {

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
    @RabbitListener(queues = "${rabbit.group-updates-queue-name}")
    public void process(TelegramUpdate update) {
        log.debug("Group update is: {}", update);
        switch (update.updateType()) {
            case GROUP_MEMBER_EVENT -> processGroupMemberEvent(update.groupMemberEvent());
            case GROUP_MESSAGE -> processGroupMessage(update.message());
            default -> log.warn("Unhandled update type: {}", update);
        }
    }

    private void processGroupMemberEvent(GroupMemberEvent groupMemberEvent) {
        switch (groupMemberEvent.eventType()) {
            case GROUP_BOT_ADDED -> processGroupBotAddEvent(groupMemberEvent.user(), groupMemberEvent.chat());
            case GROUP_BOT_LEFT, GROUP_BOT_KICKED -> processGroupBotKickEvent(groupMemberEvent.chat());
        }
    }

    private void processGroupBotAddEvent(User user, Chat chat) {
        var appUser = appUserService.findById(user.id());
        if (appUser.isPresent() && appUser.get().getRole().equals(UserRole.OWNER.name())) {
            groupChatService.save(chat.id(), chat.title());
            chatModerationSettingsService.createChatConfig(chat.id(), chat.title());
        } else {
            telegramClientService.leaveFromChat(chat.id());
        }
    }

    private void processGroupBotKickEvent(Chat chat) {
        groupChatService.remove(chat.id());
        chatModerationSettingsService.deleteChatConfig(chat.id());
        log.debug("Bot kicked from chat with id: {}", chat.id());
    }

    private void processGroupMessage(Message message) {
        groupChatService.findById(message.chat().id())
                .flatMap(groupChatRecord ->
                        chatModerationSettingsService.findChatConfigById(groupChatRecord.getId()))
                .ifPresent(chatSettings ->
                        processTextMessage(message, chatSettings.getTextModerationSettings()));
    }

    @Timed("groupTextMessageProcessing")
    private void processTextMessage(Message message, TextModerationSettings settings) {
        log.debug("Start processing message with id={}", message.messageId());
        textFilters.stream()
                .map(filter -> filter.process(message, settings))
                .filter(result -> result != TextProcessingResult.OK)
                .findFirst()
                .ifPresent(result -> {
                    telegramClientService.deleteMessage(message.chat().id(), message.messageId());
                    reportService.saveReport(message, result);
                });
        log.debug("Processed message id={}", message.messageId());
    }
}
