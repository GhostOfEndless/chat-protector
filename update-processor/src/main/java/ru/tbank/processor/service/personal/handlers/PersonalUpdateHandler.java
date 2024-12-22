package ru.tbank.processor.service.personal.handlers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.common.telegram.CallbackEvent;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.TelegramUpdate;
import ru.tbank.processor.excpetion.ButtonNotFoundException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
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

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Slf4j
@NullMarked
@RequiredArgsConstructor
public abstract class PersonalUpdateHandler {

    private static final String START_COMMAND = "/start";

    protected final PersonalChatService personalChatService;
    protected final CallbackAnswerSender callbackSender;
    protected final MessageSender messageSender;

    @Getter
    protected final UserState processedUserState;

    protected final Supplier<MessagePayload> chatNotFoundMessage = () -> MessagePayload.create(
            MessageTextCode.CHAT_MESSAGE_NOT_FOUND,
            List.of(CallbackButtonPayload.create(ButtonTextCode.BACK))
    );

    public final ProcessingResult handle(TelegramUpdate update, AppUserRecord userRecord) {
        return processUpdate(update, userRecord);
    }

    public final void goToState(AppUserRecord userRecord, Integer messageId, Object... args) {
        var messagePayload = buildMessagePayloadForUser(userRecord, args);
        messageSender.updateUserMessage(userRecord, messageId, messagePayload, processedUserState.name());
    }

    protected final ProcessingResult processUpdate(TelegramUpdate update, AppUserRecord userRecord) {
        return switch (update.updateType()) {
            case PERSONAL_MESSAGE -> processTextMessageUpdate(update.message(), userRecord);
            case CALLBACK_EVENT -> processCallbackUpdate(update.callbackEvent(), userRecord);
            default -> ProcessingResult.create(processedUserState);
        };
    }

    private ProcessingResult processCallbackUpdate(
            CallbackEvent callbackEvent,
            AppUserRecord userRecord
    ) {
        var callbackMessageId = callbackEvent.messageId();
        Integer lastMessageId = personalChatService.findByUserId(userRecord.getId())
                .orElseThrow()
                .getLastMessageId();
        if (!Objects.equals(callbackMessageId, lastMessageId)) {
            callbackSender.showMessageExpiredCallback(userRecord.getLocale(), callbackEvent.id());
            return ProcessingResult.create(UserState.NONE);
        }
        try {
            var callbackData = TelegramUtils.parseCallbackData(callbackEvent);
            return processCallbackButtonUpdate(callbackData, userRecord);
        } catch (ButtonNotFoundException e) {
            log.warn(e.getMessage());
            return ProcessingResult.create(processedUserState);
        }
    }

    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        return ProcessingResult.create(processedUserState);
    }

    protected ProcessingResult processTextMessageUpdate(Message message, AppUserRecord userRecord) {
        if (message.hasText() && message.text().startsWith(START_COMMAND)) {
            return ProcessingResult.create(UserState.START);
        }
        return ProcessingResult.create(processedUserState);
    }

    protected abstract MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args);

    protected final ProcessingResult checkPermissionAndProcess(
            UserRole requiredRole,
            AppUserRecord userRecord,
            Supplier<ProcessingResult> supplier,
            CallbackData callbackData
    ) {
        UserRole userRole = UserRole.getRoleByName(userRecord.getRole());
        if (!requiredRole.isEqualOrLowerThan(userRole)) {
            callbackSender.showPermissionDeniedCallback(userRecord.getLocale(), callbackData.callbackId());
            return ProcessingResult.create(UserState.START, callbackData.messageId());
        }
        return supplier.get();
    }
}
