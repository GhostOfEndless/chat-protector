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

import java.util.Collections;
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
            case USER -> new MessagePayload(
                    MessageTextCode.START_MESSAGE_USER,
                    Collections.emptyList()
            );
            case ADMIN -> new MessagePayload(
                    MessageTextCode.START_MESSAGE_ADMIN,
                    List.of(
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_CHATS),
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_ACCOUNT)
                    )
            );
            case OWNER -> new MessagePayload(
                    MessageTextCode.START_MESSAGE_OWNER,
                    List.of(
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_CHATS),
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_ADMINS),
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_ACCOUNT)
                    )
            );
        };
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        var callbackMessageId = callbackQuery.getMessage().getMessageId();
        var pressedButton = ButtonTextCode.valueOf(callbackQuery.getData());

        return switch (pressedButton) {
            case START_BUTTON_CHATS -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> new ProcessingResult(UserState.CHATS, callbackMessageId, new Object[]{}),
                    callbackQuery
            );
            case START_BUTTON_ADMINS -> checkPermissionAndProcess(
                    UserRole.OWNER,
                    userRecord,
                    () -> new ProcessingResult(UserState.ADMINS, callbackMessageId, new Object[]{}),
                    callbackQuery
            );

            case START_BUTTON_ACCOUNT -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> new ProcessingResult(UserState.ADMIN, callbackMessageId, new Object[]{}),
                    callbackQuery
            );
            default -> new ProcessingResult(processedUserState, callbackMessageId, new Object[]{});
        };
    }
}
