package ru.tbank.processor.service.group;

import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.tbank.common.entity.ChatModerationSettings;
import ru.tbank.common.entity.enums.TextProcessingResult;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.common.entity.spam.SpamProtectionSettings;
import ru.tbank.common.telegram.Chat;
import ru.tbank.common.telegram.GroupMemberEvent;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.TelegramUpdate;
import ru.tbank.common.telegram.User;
import ru.tbank.processor.entity.ChatUser;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.group.filter.text.TextFilter;
import ru.tbank.processor.service.moderation.ChatModerationSettingsService;
import ru.tbank.processor.service.moderation.ChatUsersService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.GroupChatService;

@Slf4j
@Service
@NullMarked
@RequiredArgsConstructor
public class GroupChatUpdateProcessingService {

    private final TelegramClientService telegramClientService;
    private final ChatModerationSettingsService chatModerationSettingsService;
    private final ChatUsersService chatUsersService;
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
            default -> throw new IllegalArgumentException("Unsupported groupMemberEventType!");
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
                        processTextMessage(message, chatSettings));
    }

    @Timed("groupTextMessageProcessing")
    private void processTextMessage(Message message, ChatModerationSettings chatModerationSettings) {
        log.debug("Start processing message with id={}", message.messageId());
        // *** text filters processing ***
        var textModerationSettings = chatModerationSettings.getTextModerationSettings();
        textFilters.stream()
                .map(filter -> filter.process(message, textModerationSettings))
                .filter(result -> result != TextProcessingResult.OK)
                .findFirst()
                .ifPresent(result -> {
                    telegramClientService.deleteMessage(message.chat().id(), message.messageId());
                    reportService.saveReport(message, result);
                });
        // ** spam protection processing ***
        var spamProtectionSettings = chatModerationSettings.getSpamProtectionSettings();
        if (spamProtectionSettings.isEnabled()) {
            processSpamProtection(message, spamProtectionSettings);
        }
        log.debug("Processed message id={}", message.messageId());
    }

    protected void processSpamProtection(Message message, SpamProtectionSettings spamProtectionSettings) {
        if (!spamProtectionSettings.getExclusions().contains(message.user().id())) {
            chatUsersService.findChatUser(message.chat().id(), message.user().id())
                    .ifPresentOrElse(chatUser -> {
                        var coolDownPeriod = Duration.ofSeconds(spamProtectionSettings.getCoolDownPeriod());
                        var currentPeriod = Duration.between(
                                chatUser.getLastProcessedMessageDt(),
                                OffsetDateTime.now(ZoneOffset.UTC)
                        );
                        if (currentPeriod.compareTo(coolDownPeriod) <= 0) {
                            telegramClientService.deleteMessage(message.chat().id(), message.messageId());
                            reportService.saveSpamReport(message);
                        } else {
                            chatUser.setLastProcessedMessageDt(OffsetDateTime.now(ZoneOffset.UTC));
                            chatUsersService.updateChatUser(chatUser);
                        }
                    }, () -> {
                        var chatUser = ChatUser.builder()
                                .userId(message.user().id())
                                .chatId(message.chat().id())
                                .lastProcessedMessageDt(OffsetDateTime.now(ZoneOffset.UTC))
                                .build();
                        chatUsersService.updateChatUser(chatUser);
                    });
        }
    }
}
