package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessageArgument;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.List;
import java.util.Objects;

@Slf4j
@NullMarked
@Component
public final class AccountStateHandler extends PersonalUpdateHandler {

    public AccountStateHandler(
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService
    ) {
        super(personalChatService, telegramClientService, textResourceService, UserState.ACCOUNT);
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        String username = userRecord.getUsername();
        if (username.isBlank()) {
            return createEmptyUsernamePayload();
        }
        if (Objects.isNull(userRecord.getHashedPassword())) {
            return createEmptyPasswordPayload(username);
        }
        return createDefaultPayload(username);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer messageId = callbackData.messageId();

        return switch (callbackData.pressedButton()) {
            case BUTTON_BACK -> ProcessingResult.create(UserState.START, messageId);
            case ACCOUNT_BUTTON_CHANGE_PASSWORD -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> ProcessingResult.create(UserState.CHANGE_PASSWORD, messageId),
                    callbackData
            );
            default -> ProcessingResult.create(processedUserState, messageId);
        };
    }

    private MessagePayload createEmptyUsernamePayload() {
        return MessagePayload.create(
                MessageTextCode.ACCOUNT_MESSAGE_USERNAME_ERROR,
                List.of(
                        CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK)
                )
        );
    }

    private MessagePayload createEmptyPasswordPayload(String username) {
        return MessagePayload.create(
                MessageTextCode.ACCOUNT_MESSAGE_PASSWORD_NOT_SET,
                List.of(
                        MessageArgument.createTextArgument(username)
                ),
                List.of(
                        CallbackButtonPayload.create(ButtonTextCode.ACCOUNT_BUTTON_CHANGE_PASSWORD),
                        CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK)
                )
        );
    }

    private MessagePayload createDefaultPayload(String username) {
        return MessagePayload.create(
                MessageTextCode.ACCOUNT_MESSAGE,
                List.of(
                        MessageArgument.createTextArgument(username)
                ),
                List.of(
                        CallbackButtonPayload.create(ButtonTextCode.ACCOUNT_BUTTON_CHANGE_PASSWORD),
                        CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK)
                )
        );
    }
}
