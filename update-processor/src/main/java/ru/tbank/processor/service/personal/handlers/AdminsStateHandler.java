package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.CallbackAnswerSender;
import ru.tbank.processor.service.personal.MessageSender;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;
import ru.tbank.processor.utils.TelegramUtils;

@Slf4j
@NullMarked
@Component
public final class AdminsStateHandler extends PersonalUpdateHandler {

    private final AppUserService appUserService;

    public AdminsStateHandler(
            PersonalChatService personalChatService,
            CallbackAnswerSender callbackSender,
            MessageSender messageSender,
            AppUserService appUserService
    ) {
        super(personalChatService, callbackSender, messageSender, UserState.ADMINS);
        this.appUserService = appUserService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        var admins = appUserService.findAllAdmins();
        var adminsButtons = TelegramUtils.buildUserButtons(admins);
        adminsButtons.add(CallbackButtonPayload.create(ButtonTextCode.ADMINS_BUTTON_ADMIN_ADDITION));
        adminsButtons.add(CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK));
        return MessagePayload.create(MessageTextCode.ADMINS_MESSAGE, adminsButtons);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer messageId = callbackData.messageId();
        return switch (callbackData.pressedButton()) {
            case BUTTON_BACK -> ProcessingResult.create(UserState.START, messageId);
            case ADMINS_BUTTON_ADMIN_ADDITION -> checkPermissionAndProcess(
                    UserRole.OWNER,
                    userRecord,
                    () -> ProcessingResult.create(UserState.ADMIN_ADDITION, messageId),
                    callbackData
            );
            case ADMINS_BUTTON_ADMIN -> checkPermissionAndProcess(
                    UserRole.OWNER,
                    userRecord,
                    () -> ProcessingResult.create(UserState.ADMIN, messageId, callbackData.getAdminId()),
                    callbackData
            );
            default -> ProcessingResult.create(processedUserState, messageId);
        };
    }
}
