package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserRole;
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
public final class AdminStateHandler extends PersonalUpdateHandler {

    private final AppUserService appUserService;

    public AdminStateHandler(
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService,
            AppUserService appUserService) {
        super(personalChatService, telegramClientService, textResourceService, UserState.ADMIN);
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
                                CallbackButtonPayload.create(ButtonTextCode.ADMIN_BUTTON_REMOVE, userId),
                                CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK)
                        )))
                .orElseGet(() -> MessagePayload.create(
                        MessageTextCode.ADMIN_MESSAGE_NOT_FOUND,
                        List.of(CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK))
                ));
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer messageId = callbackData.messageId();

        return switch (callbackData.pressedButton()) {
            case BUTTON_BACK -> ProcessingResult.create(UserState.ADMINS, messageId);
            case ADMIN_BUTTON_REMOVE -> checkPermissionAndProcess(
                    UserRole.OWNER,
                    userRecord,
                    () -> {
                        appUserService.updateUserRole(callbackData.getAdminId(), UserRole.USER.name());
                        return ProcessingResult.create(UserState.ADMINS, messageId);
                    },
                    callbackData
            );
            default -> ProcessingResult.create(UserState.START, messageId);
        };
    }
}
