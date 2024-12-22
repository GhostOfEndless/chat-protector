package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.CallbackAnswerSender;
import ru.tbank.processor.service.personal.MessageSender;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessageArgument;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.List;

@NullMarked
@Slf4j
@Component
public final class ChatStateHandler extends PersonalUpdateHandler {

    private final GroupChatService groupChatService;

    public ChatStateHandler(
            PersonalChatService personalChatService,
            GroupChatService groupChatService,
            CallbackAnswerSender callbackSender,
            MessageSender messageSender
    ) {
        super(personalChatService, callbackSender, messageSender, UserState.CHAT);
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        long chatId = (Long) args[0];
        return groupChatService.findById(chatId)
                .map(chatRecord -> MessagePayload.create(
                        MessageTextCode.CHAT_MESSAGE,
                        List.of(
                                MessageArgument.createTextArgument(chatRecord.getName())
                        ),
                        List.of(
                                CallbackButtonPayload.create(ButtonTextCode.CHAT_FILTERS_SETTINGS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.CHAT_EXCLUDE, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.BACK)
                        )))
                .orElseGet(chatNotFoundMessage);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer messageId = callbackData.messageId();

        return switch (callbackData.pressedButton()) {
            case BACK -> ProcessingResult.create(UserState.CHATS, messageId);
            case CHAT_FILTERS_SETTINGS -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> ProcessingResult.create(UserState.FILTERS, messageId, callbackData.getChatId()),
                    callbackData
            );
            case CHAT_EXCLUDE -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> ProcessingResult.create(UserState.CHAT_DELETION, messageId, callbackData.getChatId()),
                    callbackData
            );
            default -> ProcessingResult.create(UserState.START, messageId);
        };
    }
}
