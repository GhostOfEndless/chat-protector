package ru.tbank.processor.service.personal.handlers;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.moderation.ChatModerationSettingsService;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.CallbackAnswerSender;
import ru.tbank.processor.service.personal.MessageSender;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackAnswerPayload;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

@NullMarked
@Slf4j
@Component
public final class ChatDeletionStateHandler extends PersonalUpdateHandler {

    private final GroupChatService groupChatService;
    private final ChatModerationSettingsService chatModerationSettingsService;
    private final TelegramClientService telegramClientService;

    public ChatDeletionStateHandler(
            PersonalChatService personalChatService,
            GroupChatService groupChatService,
            CallbackAnswerSender callbackSender,
            MessageSender messageSender,
            ChatModerationSettingsService chatModerationSettingsService,
            TelegramClientService telegramClientService
    ) {
        super(personalChatService, callbackSender, messageSender, UserState.CHAT_DELETION);
        this.chatModerationSettingsService = chatModerationSettingsService;
        this.groupChatService = groupChatService;
        this.telegramClientService = telegramClientService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        long chatId = (Long) args[0];
        return groupChatService.findById(chatId)
                .map(chatRecord -> MessagePayload.create(
                        MessageTextCode.CHAT_DELETION_MESSAGE,
                        List.of(
                                CallbackButtonPayload.create(ButtonTextCode.CHAT_DELETION_CONFIRM, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.BACK, chatId)
                        )))
                .orElseGet(chatNotFoundMessage);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Long chatId = callbackData.getChatId();
        Integer messageId = callbackData.messageId();
        String callbackId = callbackData.callbackId();
        String userLocale = userRecord.getLocale();

        if (chatId == 0) {
            callbackSender.showChatUnavailableCallback(callbackId, userLocale);
            return ProcessingResult.create(UserState.CHATS, messageId);
        }

        return switch (callbackData.pressedButton()) {
            case BACK -> ProcessingResult.create(UserState.CHAT, messageId, chatId);
            case CHAT_DELETION_CONFIRM -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> {
                        telegramClientService.leaveFromChat(chatId);
                        showChatRemovedCallback(userLocale, callbackId);
                        chatModerationSettingsService.deleteChatConfig(chatId);
                        groupChatService.remove(chatId);

                        log.debug("Moderation config of chat with id={} was deleted", chatId);
                        return ProcessingResult.create(UserState.CHATS, messageId, chatId);
                    },
                    callbackData
            );
            default -> ProcessingResult.create(UserState.START, messageId);
        };
    }

    private void showChatRemovedCallback(String userLocale, String callbackId) {
        callbackSender.showAnswerCallback(
                CallbackAnswerPayload.create(
                        CallbackTextCode.CHAT_REMOVED
                ),
                userLocale,
                callbackId
        );
    }
}
