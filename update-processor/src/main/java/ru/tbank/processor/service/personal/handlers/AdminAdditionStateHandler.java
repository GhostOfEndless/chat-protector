package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.group.filter.text.TextEntityType;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.AdminAdditionResult;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserRole;
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
            TelegramClientService telegramClientService,
            TextResourceService textResourceService,
            AppUserService appUserService) {
        super(personalChatService, telegramClientService, textResourceService, UserState.ADMIN_ADDITION);
        this.appUserService = appUserService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        var backButton = List.of(
                CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK)
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
            var messageEntity = message.getEntities().stream()
                    .filter(entity -> TextEntityType.MENTION.isTypeOf(entity.getType()))
                    .findFirst();

            if (messageEntity.isPresent()) {
                String username = messageEntity.get().getText().substring(1);
                var addedUser = appUserService.findByUsername(username);
                if (addedUser.isPresent()) {
                    appUserService.updateUserRole(addedUser.get().getId(), UserRole.ADMIN.name());
                    return ProcessingResult.create(processedUserState, 0, AdminAdditionResult.SUCCESS);
                } else {
                    return ProcessingResult.create(processedUserState, 0, AdminAdditionResult.USER_NOT_FOUND);
                }
            }
        }
        return super.processTextMessageUpdate(message, userRecord);
    }
}
