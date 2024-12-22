package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.enums.MessageEntityType;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.CallbackAnswerSender;
import ru.tbank.processor.service.personal.MessageSender;
import ru.tbank.processor.service.personal.enums.AdminAdditionResult;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.List;

@Slf4j
@NullMarked
@Component
public final class AdminAdditionStateHandler extends PersonalUpdateHandler {

    private final AppUserService appUserService;

    public AdminAdditionStateHandler(
            PersonalChatService personalChatService,
            CallbackAnswerSender callbackSender,
            MessageSender messageSender,
            AppUserService appUserService
    ) {
        super(personalChatService, callbackSender, messageSender, UserState.ADMIN_ADDITION);
        this.appUserService = appUserService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        var backButton = List.of(
                CallbackButtonPayload.create(ButtonTextCode.BACK)
        );

        if (args.length > 0 && AdminAdditionResult.isAdditionResult(args[0])) {
            return switch ((AdminAdditionResult) args[0]) {
                case SUCCESS -> MessagePayload.create(
                        MessageTextCode.ADMIN_ADDITION_MESSAGE_SUCCESS,
                        backButton
                );
                case USER_NOT_FOUND -> MessagePayload.create(
                        MessageTextCode.ADMIN_ADDITION_MESSAGE_USER_NOT_FOUND,
                        backButton
                );
                case USER_IS_ADMIN -> MessagePayload.create(
                        MessageTextCode.ADMIN_ADDITION_MESSAGE_USER_IS_ADMIN,
                        backButton
                );
            };
        }
        return MessagePayload.create(
                MessageTextCode.ADMIN_ADDITION_MESSAGE,
                backButton
        );
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer messageId = callbackData.messageId();

        if (!callbackData.pressedButton().isBackButton()) {
            return ProcessingResult.create(processedUserState, messageId);
        }
        return ProcessingResult.create(UserState.ADMINS, messageId);
    }

    @Override
    protected ProcessingResult processTextMessageUpdate(Message message, AppUserRecord userRecord) {
        if (message.hasEntities()) {
            var messageEntity = message.entities().stream()
                    .filter(entity -> entity.type() == MessageEntityType.MENTION)
                    .findFirst();

            if (messageEntity.isPresent()) {
                String username = messageEntity.get().text().substring(1);
                var addedUser = appUserService.findByUsername(username);

                if (addedUser.isEmpty()) {
                    goToState(userRecord, 0, AdminAdditionResult.USER_NOT_FOUND);
                    return ProcessingResult.create(processedUserState);
                }
                if (UserRole.ADMIN.isEqualOrLowerThan(UserRole.getRoleByName(addedUser.get().getRole()))) {
                    goToState(userRecord, 0, AdminAdditionResult.USER_IS_ADMIN);
                    return ProcessingResult.create(processedUserState);
                }
                appUserService.updateUserRole(addedUser.get().getId(), UserRole.ADMIN.name());
                goToState(userRecord, 0, AdminAdditionResult.SUCCESS);
                return ProcessingResult.create(processedUserState);
            }
        }
        return super.processTextMessageUpdate(message, userRecord);
    }
}
