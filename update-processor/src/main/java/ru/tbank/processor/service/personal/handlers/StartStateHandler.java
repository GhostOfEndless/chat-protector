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
            case USER -> MessagePayload.builder()
                    .messageText(MessageTextCode.START_MESSAGE_USER)
                    .buttons(Collections.emptyList())
                    .build();
            case ADMIN -> MessagePayload.builder()
                    .messageText(MessageTextCode.START_MESSAGE_ADMIN)
                    .buttons(List.of(
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_CHATS),
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_ACCOUNT)
                    ))
                    .build();
            case OWNER -> MessagePayload.builder()
                    .messageText(MessageTextCode.START_MESSAGE_OWNER)
                    .buttons(List.of(
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_CHATS),
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_ADMINS),
                            CallbackButtonPayload.create(ButtonTextCode.START_BUTTON_ACCOUNT)
                    ))
                    .build();
        };
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        var callbackQueryId = callbackQuery.getId();
        var callbackMessageId = callbackQuery.getMessage().getMessageId();
        var pressedButton = ButtonTextCode.valueOf(callbackQuery.getData());
        UserRole userRole = UserRole.valueOf(userRecord.getRole());

        return switch (pressedButton) {
            case START_BUTTON_CHATS -> {
                if (UserRole.ADMIN.isEqualOrLowerThan(userRole)) {
                    yield new ProcessingResult(UserState.CHATS, callbackMessageId, new Object[]{});
                } else {
                    showPermissionDeniedCallback(userRecord.getLocale(), callbackQueryId);
                    yield new ProcessingResult(processedUserState, callbackMessageId, new Object[]{});
                }
            }
            case START_BUTTON_ADMINS -> {
                if (UserRole.OWNER.isEqualOrLowerThan(userRole)) {
                    yield new ProcessingResult(UserState.ADMINS, callbackMessageId, new Object[]{});
                } else {
                    showPermissionDeniedCallback(userRecord.getLocale(), callbackQueryId);
                    yield new ProcessingResult(processedUserState, callbackMessageId, new Object[]{});
                }
            }
            case START_BUTTON_ACCOUNT -> {
                if (UserRole.ADMIN.isEqualOrLowerThan(userRole)) {
                    yield new ProcessingResult(UserState.ADMIN, callbackMessageId, new Object[]{});
                } else {
                    showPermissionDeniedCallback(userRecord.getLocale(), callbackQueryId);
                    yield new ProcessingResult(processedUserState, callbackMessageId, new Object[]{});
                }
            }
            default -> new ProcessingResult(processedUserState, callbackMessageId, new Object[]{});
        };
    }
}
