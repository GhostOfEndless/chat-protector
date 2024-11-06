package ru.tbank.processor.service.personal.handlers.impl;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.generated.tables.records.PersonalChatRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserRole;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.handlers.PersonalUpdateHandler;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.utils.UpdateType;

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
    protected void processUpdate(UpdateType updateType, Update update, AppUserRecord userRecord) {
        if (updateType == UpdateType.PERSONAL_MESSAGE) {
            if (update.getMessage().getText().startsWith("/start")) {
                goToState(userRecord, 0);
            }
        } else if (updateType == UpdateType.CALLBACK) {
            var callbackQuery = update.getCallbackQuery();
            processCallbackButton(callbackQuery, userRecord);
        }
    }

    @Override
    protected void goToState(AppUserRecord userRecord, Integer messageId) {
        var messagePayload = buildMessagePayloadForUser(userRecord);
        var keyboardMarkup = buildKeyboard(messagePayload.buttons(), userRecord.getLocale());

        try {
            if (messageId == 0) {
                Message sentMessage = telegramClientService.sendMessage(
                        userRecord.getId(),
                        textResourceService.getMessageText(messagePayload.messageText(), userRecord.getLocale()),
                        keyboardMarkup
                );
                personalChatService.save(userRecord.getId(), UserState.START.name(), sentMessage.getMessageId());
            } else {
                telegramClientService.editMessage(
                        userRecord.getId(),
                        messageId,
                        textResourceService.getMessageText(messagePayload.messageText(), userRecord.getLocale()),
                        keyboardMarkup
                );
                personalChatService.save(userRecord.getId(), UserState.START.name(), messageId);
            }
        } catch (TelegramApiException e) {
            log.error("Telegram API Error: {}", e.getMessage());
        }
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord) {
        UserRole userRole = UserRole.valueOf(userRecord.getRole());
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

    private void processCallbackButton(
            CallbackQuery callbackQuery,
            AppUserRecord userRecord
    ) {
        var callbackQueryId = callbackQuery.getId();
        var callbackMessageId = callbackQuery.getMessage().getMessageId();
        var chatLastMessageId = personalChatService.findByUserId(userRecord.getId())
                .orElse(new PersonalChatRecord())
                .getLastMessageId();

        if (isRemovedExpiredMessage(userRecord, callbackQueryId, chatLastMessageId, callbackMessageId)) {
            return;
        }

        var pressedButton = ButtonTextCode.valueOf(callbackQuery.getData());
        UserRole userRole = UserRole.valueOf(userRecord.getRole());
        boolean hasPermission = false;

        switch (pressedButton) {
            case START_BUTTON_CHATS -> {
                // TODO: тут будет логика перехода пользователя на следующее состояние
                if (UserRole.ADMIN.isEqualOrLowerThan(userRole)) {
                    // chatsStateHandler.goToState(userRecord, chatLastMessageId);
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

        if (hasPermission) {
            sendCallbackForPressedButton(
                    CallbackTextCode.BUTTON_PRESSED,
                    callbackQueryId,
                    pressedButton,
                    userRecord.getLocale()
            );
        } else {
            log.warn("Permission denied for command '{}' for user: {}", pressedButton, userRecord);
            removeMessageWithCallback(
                    chatLastMessageId,
                    userRecord,
                    CallbackTextCode.PERMISSION_DENIED,
                    callbackQueryId
            );
        }
    }
}
