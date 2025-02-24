package ru.tbank.processor.service.personal.handlers;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.persistence.GroupChatService;
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

@NullMarked
@Slf4j
@Component
public final class FiltersStateHandler extends PersonalUpdateHandler {

    private final GroupChatService groupChatService;

    public FiltersStateHandler(
            PersonalChatService personalChatService,
            GroupChatService groupChatService,
            CallbackAnswerSender callbackSender,
            MessageSender messageSender
    ) {
        super(personalChatService, callbackSender, messageSender, UserState.FILTERS);
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        long chatId = (Long) args[0];
        return groupChatService.findById(chatId)
                .map(chatRecord -> MessagePayload.create(
                        MessageTextCode.FILTERS_MESSAGE,
                        List.of(
                                CallbackButtonPayload.create(ButtonTextCode.FILTERS_TEXT_FILTERS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.BACK, chatId)
                        )))
                .orElseGet(chatNotFoundMessage);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer messageId = callbackData.messageId();
        Long chatId = callbackData.getChatId();

        if (chatId == 0) {
            callbackSender.showChatUnavailableCallback(callbackData.callbackId(), userRecord.getLocale());
            return ProcessingResult.create(UserState.CHATS, messageId);
        }

        return switch (callbackData.pressedButton()) {
            case BACK -> ProcessingResult.create(UserState.CHAT, messageId, chatId);
            case FILTERS_TEXT_FILTERS -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> ProcessingResult.create(UserState.TEXT_FILTERS, messageId, chatId),
                    callbackData
            );
            default -> ProcessingResult.create(UserState.START, messageId);
        };
    }
}
