package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.CallbackAnswerSender;
import ru.tbank.processor.service.personal.MessageSender;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.List;

@NullMarked
@Slf4j
@Component
public final class StartStateHandler extends PersonalUpdateHandler {

    public StartStateHandler(
            PersonalChatService personalChatService,
            CallbackAnswerSender callbackSender,
            MessageSender messageSender
    ) {
        super(personalChatService, callbackSender, messageSender, UserState.START);
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        UserRole userRole = UserRole.getRoleByName(userRecord.getRole());
        return switch (userRole) {
            case USER -> MessagePayload.create(
                    MessageTextCode.START_MESSAGE_USER,
                    List.of(
                            CallbackButtonPayload.create(ButtonTextCode.START_LANGUAGE)
                    )
            );
            case ADMIN -> MessagePayload.create(
                    MessageTextCode.START_MESSAGE_ADMIN,
                    List.of(
                            CallbackButtonPayload.create(ButtonTextCode.START_CHATS),
                            CallbackButtonPayload.create(ButtonTextCode.START_ACCOUNT),
                            CallbackButtonPayload.create(ButtonTextCode.START_LANGUAGE)
                    )
            );
            case OWNER -> MessagePayload.create(
                    MessageTextCode.START_MESSAGE_OWNER,
                    List.of(
                            CallbackButtonPayload.create(ButtonTextCode.START_CHATS),
                            CallbackButtonPayload.create(ButtonTextCode.START_ADMINS),
                            CallbackButtonPayload.create(ButtonTextCode.START_ACCOUNT),
                            CallbackButtonPayload.create(ButtonTextCode.START_LANGUAGE)
                    )
            );
        };
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer messageId = callbackData.messageId();
        return switch (callbackData.pressedButton()) {
            case START_LANGUAGE -> ProcessingResult.create(UserState.LANGUAGE, messageId);
            case START_CHATS -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> ProcessingResult.create(UserState.CHATS, messageId),
                    callbackData
            );
            case START_ADMINS -> checkPermissionAndProcess(
                    UserRole.OWNER,
                    userRecord,
                    () -> ProcessingResult.create(UserState.ADMINS, messageId),
                    callbackData
            );
            case START_ACCOUNT -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> ProcessingResult.create(UserState.ACCOUNT, messageId),
                    callbackData
            );
            default -> ProcessingResult.create(processedUserState, messageId);
        };
    }
}
