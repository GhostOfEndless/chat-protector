package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserRole;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
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
    protected MessagePayload buildMessagePayloadForUser(UserRole userRole, Object[] args) {
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
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_ACCOUNT),
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_LANGUAGE)
                    )
            );
            case OWNER -> MessagePayload.create(
                    MessageTextCode.START_MESSAGE_OWNER,
                    List.of(
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_CHATS),
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_ADMINS),
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_ACCOUNT),
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_LANGUAGE)
                    )
            );
        };
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        var callbackMessageId = callbackQuery.getMessage().getMessageId();
        var pressedButton = ButtonTextCode.valueOf(callbackQuery.getData());

        return switch (pressedButton) {
            case START_BUTTON_LANGUAGE -> ProcessingResult.create(UserState.LANGUAGE, callbackMessageId);
            case START_BUTTON_CHATS -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> ProcessingResult.create(UserState.CHATS, callbackMessageId),
                    callbackQuery
            );
            case START_BUTTON_ADMINS -> checkPermissionAndProcess(
                    UserRole.OWNER,
                    userRecord,
                    () -> ProcessingResult.create(UserState.ADMINS, callbackMessageId),
                    callbackQuery
            );
            case START_BUTTON_ACCOUNT -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> ProcessingResult.create(UserState.ADMIN, callbackMessageId),
                    callbackQuery
            );
            default -> ProcessingResult.create(processedUserState, callbackMessageId);
        };
    }
}
