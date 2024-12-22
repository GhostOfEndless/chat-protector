package ru.tbank.processor.service.personal.handlers;

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

import java.util.List;

@NullMarked
@Slf4j
@Component
public final class TextFiltersStateHandler extends PersonalUpdateHandler {

    private final GroupChatService groupChatService;

    public TextFiltersStateHandler(
            PersonalChatService personalChatService,
            GroupChatService groupChatService,
            CallbackAnswerSender callbackSender,
            MessageSender messageSender
    ) {
        super(personalChatService, callbackSender, messageSender, UserState.TEXT_FILTERS);
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        long chatId = (Long) args[0];
        return groupChatService.findById(chatId)
                .map(chatRecord -> MessagePayload.create(
                        MessageTextCode.TEXT_FILTERS_MESSAGE,
                        List.of(
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_TAGS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_EMAILS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_LINKS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_MENTIONS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_BOT_COMMANDS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_CUSTOM_EMOJIS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_PHONE_NUMBERS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.BACK, chatId)
                        )
                ))
                .orElseGet(chatNotFoundMessage);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        long chatId = callbackData.getChatId();
        var pressedButton = callbackData.pressedButton();
        Integer messageId = callbackData.messageId();

        if (chatId == 0) {
            callbackSender.showChatUnavailableCallback(callbackData.callbackId(), userRecord.getLocale());
            return ProcessingResult.create(UserState.CHATS, messageId);
        }
        if (pressedButton.isBackButton()) {
            return ProcessingResult.create(UserState.FILTERS, messageId, chatId);
        }
        if (!pressedButton.isButtonFilterType()) {
            return ProcessingResult.create(UserState.START, messageId);
        }

        var filterType = pressedButton.getFilterTypeFromButton();
        return checkPermissionAndProcess(
                UserRole.ADMIN,
                userRecord,
                () -> ProcessingResult.create(UserState.TEXT_FILTER, messageId, chatId, filterType),
                callbackData
        );
    }
}
