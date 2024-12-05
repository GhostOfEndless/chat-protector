package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserRole;
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
            TelegramClientService telegramClientService,
            TextResourceService textResourceService
    ) {
        super(personalChatService, telegramClientService, textResourceService, UserState.START);
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        UserRole userRole = UserRole.getRoleByName(userRecord.getRole());
        return switch (userRole) {
            case USER -> MessagePayload.create(
                    MessageTextCode.START_MESSAGE_USER,
                    List.of(
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_LANGUAGE)
                    )
            );
            case ADMIN -> MessagePayload.create(
                    MessageTextCode.START_MESSAGE_ADMIN,
                    List.of(
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_CHATS),
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_LANGUAGE)
                    )
            );
            case OWNER -> MessagePayload.create(
                    MessageTextCode.START_MESSAGE_OWNER,
                    List.of(
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_CHATS),
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_ADMINS),
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_LANGUAGE)
                    )
            );
        };
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer messageId = callbackData.messageId();
        return switch (callbackData.pressedButton()) {
            case START_BUTTON_LANGUAGE -> ProcessingResult.create(UserState.LANGUAGE, messageId);
            case START_BUTTON_CHATS -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> ProcessingResult.create(UserState.CHATS, messageId),
                    callbackData
            );
            case START_BUTTON_ADMINS -> checkPermissionAndProcess(
                    UserRole.OWNER,
                    userRecord,
                    () -> ProcessingResult.create(UserState.ADMINS, messageId),
                    callbackData
            );
            case START_BUTTON_ACCOUNT -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> ProcessingResult.create(UserState.ADMIN, messageId),
                    callbackData
            );
            default -> ProcessingResult.create(processedUserState, messageId);
        };
    }
}
