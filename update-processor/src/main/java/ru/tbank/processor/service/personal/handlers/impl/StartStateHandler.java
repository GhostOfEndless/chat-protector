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
import ru.tbank.processor.utils.UpdateType;

import java.util.Collections;
import java.util.List;

@NullMarked
@Slf4j
@Component
public class StartStateHandler extends PersonalUpdateHandler {

    public StartStateHandler(
            AppUserService appUserService,
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService
    ) {
        super(appUserService, personalChatService, telegramClientService, textResourceService, UserState.START);
    }

    @Override
    protected void processUpdate(UpdateType updateType, Update update, AppUserRecord userRecord) {
        if (updateType == UpdateType.PERSONAL_MESSAGE) {
            if (update.getMessage().getText().startsWith("/start")) {
                processStartCommand(userRecord);
            }
        } else if (updateType == UpdateType.CALLBACK) {
            var callbackQuery = update.getCallbackQuery();
            processCallbackButton(callbackQuery, userRecord);
        }
    }

    private void processStartCommand(AppUserRecord userRecord) {
        UserRole userRole = UserRole.valueOf(userRecord.getRole());
        switch (userRole) {
            case USER -> sendStartStateMessage(MessageTextCode.START_MESSAGE_USER, Collections.emptyList(), userRecord);
            case ADMIN -> sendStartStateMessage(
                    MessageTextCode.START_MESSAGE_ADMIN,
                    List.of(
                            ButtonTextCode.START_BUTTON_CHATS,
                            ButtonTextCode.START_BUTTON_ACCOUNT
                    ),
                    userRecord
            );
            case OWNER -> sendStartStateMessage(
                    MessageTextCode.START_MESSAGE_OWNER,
                    List.of(
                            ButtonTextCode.START_BUTTON_ADMINS,
                            ButtonTextCode.START_BUTTON_CHATS,
                            ButtonTextCode.START_BUTTON_ACCOUNT
                    ),
                    userRecord
            );
        }
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

        boolean hasPermission = switch (pressedButton) {
            case START_BUTTON_CHATS ->
                    // TODO: тут будет логика перехода пользователя на следующее состояние
                    UserRole.ADMIN.isEqualOrLowerThan(userRole);
            case START_BUTTON_ADMINS ->
                    UserRole.ADMIN.isEqualOrLowerThan(userRole);
            case START_BUTTON_ACCOUNT ->
                    UserRole.ADMIN.isEqualOrLowerThan(userRole);
        };

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

    private void sendStartStateMessage(
            MessageTextCode messageText,
            List<ButtonTextCode> buttonsText,
            AppUserRecord userRecord
    ) {
        var keyboardMarkup = buildKeyboard(buttonsText, userRecord.getLocale());

        try {
            Message sentMessage = telegramClientService.sendMessage(
                    userRecord.getId(),
                    textResourceService.getMessageText(messageText, userRecord.getLocale()),
                    keyboardMarkup
            );
            personalChatService.save(userRecord.getId(), UserState.START.name(), sentMessage.getMessageId());
        } catch (TelegramApiException e) {
            log.error("Telegram API Error: {}", e.getMessage());
        }
    }
}
