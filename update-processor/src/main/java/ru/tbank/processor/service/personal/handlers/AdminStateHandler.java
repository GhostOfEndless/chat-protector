package ru.tbank.processor.service.personal.handlers;

import java.util.List;
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
import ru.tbank.processor.service.personal.payload.MessageArgument;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

@Slf4j
@NullMarked
@Component
public final class AdminStateHandler extends PersonalUpdateHandler {

    private final AppUserService appUserService;

    public AdminStateHandler(
            PersonalChatService personalChatService,
            CallbackAnswerSender callbackSender,
            MessageSender messageSender,
            AppUserService appUserService
    ) {
        super(personalChatService, callbackSender, messageSender, UserState.ADMIN);
        this.appUserService = appUserService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        Long userId = (Long) args[0];
        return appUserService.findById(userId)
                .map(adminRecord -> MessagePayload.create(
                        MessageTextCode.ADMIN_MESSAGE,
                        List.of(
                                MessageArgument.createTextArgument(
                                        "%s %s".formatted(adminRecord.getFirstName(), adminRecord.getLastName())
                                )
                        ),
                        List.of(
                                CallbackButtonPayload.create(ButtonTextCode.ADMIN_REMOVE, userId),
                                CallbackButtonPayload.create(ButtonTextCode.BACK)
                        )))
                .orElseGet(() -> MessagePayload.create(
                        MessageTextCode.ADMIN_MESSAGE_NOT_FOUND,
                        List.of(CallbackButtonPayload.create(ButtonTextCode.BACK))
                ));
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer messageId = callbackData.messageId();

        return switch (callbackData.pressedButton()) {
            case BACK -> ProcessingResult.create(UserState.ADMINS, messageId);
            case ADMIN_REMOVE -> checkPermissionAndProcess(
                    UserRole.OWNER,
                    userRecord,
                    () -> {
                        appUserService.updateUserRole(callbackData.getAdminId(), UserRole.USER.name());
                        return ProcessingResult.create(UserState.ADMINS, messageId);
                    },
                    callbackData
            );
            default -> ProcessingResult.create(processedUserState, messageId);
        };
    }
}
