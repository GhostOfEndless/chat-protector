package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.PasswordGenerator;
import ru.tbank.processor.service.persistence.AppUserService;
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

@Slf4j
@NullMarked
@Component
public final class ChangePasswordStateHandler extends PersonalUpdateHandler {

    private final PasswordGenerator passwordGenerator;
    private final AppUserService appUserService;

    public ChangePasswordStateHandler(
            PersonalChatService personalChatService,
            PasswordGenerator passwordGenerator,
            MessageSender messageSender,
            CallbackAnswerSender callbackSender,
            AppUserService appUserService
    ) {
        super(personalChatService, callbackSender, messageSender, UserState.CHANGE_PASSWORD);
        this.passwordGenerator = passwordGenerator;
        this.appUserService = appUserService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        String newPassword = passwordGenerator.generatePassword();
        appUserService.updatePassword(userRecord.getId(), newPassword);
        return MessagePayload.create(
                MessageTextCode.CHANGE_PASSWORD_MESSAGE,
                List.of(
                        MessageArgument.createTextArgument(userRecord.getUsername()),
                        MessageArgument.createTextArgument(newPassword)
                ),
                List.of(
                        CallbackButtonPayload.create(ButtonTextCode.BACK)
                )
        );
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer messageId = callbackData.messageId();
        if (callbackData.pressedButton().isBackButton()) {
            return ProcessingResult.create(UserState.ACCOUNT, messageId);
        }
        return ProcessingResult.create(processedUserState, messageId);
    }
}
