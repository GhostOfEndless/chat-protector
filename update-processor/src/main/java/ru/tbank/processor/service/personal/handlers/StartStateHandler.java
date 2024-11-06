package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserRole;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.MessagePayload;

import java.util.Collections;
import java.util.List;

@NullMarked
@Slf4j
@Component
public final class StartStateHandler extends PersonalUpdateHandler {

    private final ChatsStateHandler chatsStateHandler;

    public StartStateHandler(
            AppUserService appUserService,
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService,
            ChatsStateHandler chatsStateHandler
    ) {
        super(appUserService, personalChatService, telegramClientService, textResourceService, UserState.START);
        this.chatsStateHandler = chatsStateHandler;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(UserRole userRole) {
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
    protected void processTextMessageUpdate(Update update, AppUserRecord userRecord) {
        if (update.getMessage().hasText() && update.getMessage().getText().startsWith("/start")) {
            goToState(userRecord, 0);
        }
    }

    @Override
    protected void processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        var callbackQueryId = callbackQuery.getId();
        var callbackMessageId = callbackQuery.getMessage().getMessageId();
        var pressedButton = ButtonTextCode.valueOf(callbackQuery.getData());
        UserRole userRole = UserRole.valueOf(userRecord.getRole());
        boolean hasPermission = false;

        switch (pressedButton) {
            case START_BUTTON_CHATS -> {
                if (UserRole.ADMIN.isEqualOrLowerThan(userRole)) {
                    chatsStateHandler.goToState(userRecord, callbackMessageId);
                    hasPermission = true;
                }
            }
            case START_BUTTON_ADMINS -> {
                if (UserRole.OWNER.isEqualOrLowerThan(userRole)) {
                    hasPermission = true;
                    // производим какую-то операцию
                }
            }
            case START_BUTTON_ACCOUNT -> {
                if (UserRole.ADMIN.isEqualOrLowerThan(userRole)) {
                    hasPermission = true;
                    // производим какую-то операцию
                }
            }
        }

        if (!hasPermission) {
            log.warn("Permission denied for command '{}' for user with id: {}", pressedButton, userRecord.getId());
            removeMessageWithCallback(
                    callbackMessageId,
                    userRecord,
                    CallbackTextCode.PERMISSION_DENIED,
                    callbackQueryId
            );
        }
    }
}
