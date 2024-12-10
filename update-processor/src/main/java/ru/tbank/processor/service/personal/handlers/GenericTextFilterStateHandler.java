package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.common.entity.enums.FilterType;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.processor.excpetion.ChatModerationSettingsNotFoundException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.moderation.TextModerationSettingsService;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.CallbackAnswerSender;
import ru.tbank.processor.service.personal.MessageSender;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackAnswerPayload;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessageArgument;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.List;

@Slf4j
@NullMarked
@Component
public final class GenericTextFilterStateHandler extends PersonalUpdateHandler {

    private final TextModerationSettingsService textModerationSettingsService;
    private final GroupChatService groupChatService;

    public GenericTextFilterStateHandler(
            TextModerationSettingsService textModerationSettingsService,
            PersonalChatService personalChatService,
            GroupChatService groupChatService,
            CallbackAnswerSender callbackSender,
            MessageSender messageSender
    ) {
        super(personalChatService, callbackSender, messageSender, UserState.TEXT_FILTER);
        this.textModerationSettingsService = textModerationSettingsService;
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        long chatId = (Long) args[0];
        FilterType filterType = (FilterType) args[1];

        var filterSettings = textModerationSettingsService.getFilterSettings(chatId, filterType);
        var buttonText = filterSettings.isEnabled()
                ? ButtonTextCode.TEXT_FILTER_BUTTON_DISABLE
                : ButtonTextCode.TEXT_FILTER_BUTTON_ENABLE;
        return groupChatService.findById(chatId)
                .map(chatRecord -> MessagePayload.create(
                        MessageTextCode.TEXT_FILTER_MESSAGE,
                        List.of(
                                MessageArgument.createResourceArgument(MessageTextCode.getFilterNameByType(filterType))
                        ),
                        List.of(
                                CallbackButtonPayload.create(
                                        buttonText,
                                        chatId,
                                        filterType.name()
                                ),
                                CallbackButtonPayload.create(
                                        ButtonTextCode.BUTTON_BACK,
                                        chatId
                                )
                        )))
                .orElseGet(chatNotFoundMessage);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer callbackMessageId = callbackData.messageId();
        Long chatId = callbackData.getChatId();
        var pressedButton = callbackData.pressedButton();

        if (chatId == 0) {
            callbackSender.showChatUnavailableCallback(callbackData.callbackId(), userRecord.getLocale());
            return ProcessingResult.create(UserState.CHATS, callbackMessageId);
        }
        if (pressedButton.isBackButton()) {
            return ProcessingResult.create(UserState.TEXT_FILTERS, callbackMessageId, chatId);
        }
        if (!pressedButton.isFilterControlButton()) {
            return ProcessingResult.create(UserState.START, callbackMessageId);
        }

        var filterType = callbackData.getFilterType();
        return processFilterStateChanged(
                userRecord,
                filterType,
                pressedButton == ButtonTextCode.TEXT_FILTER_BUTTON_ENABLE,
                callbackData
        );
    }

    private ProcessingResult processFilterStateChanged(
            AppUserRecord userRecord,
            FilterType filterType,
            boolean newState,
            CallbackData callbackData
    ) {
        return checkPermissionAndProcess(
                UserRole.ADMIN,
                userRecord,
                () -> {
                    Integer messageId = callbackData.messageId();
                    Long chatId = callbackData.getChatId();
                    String callbackId = callbackData.callbackId();
                    String userLocale = userRecord.getLocale();
                    try {
                        log.debug("Filter type to enable is: {}", filterType.name());
                        textModerationSettingsService.updateFilterState(chatId, filterType, newState);
                        // TODO: сделать ожидание обновления в Redis с каким-то timeout,
                        //  если timeout истёк - отправить ошибку

                        showFilterStateChangedCallback(userLocale, callbackId, newState);
                        goToState(userRecord, messageId, chatId, filterType);
                    } catch (ChatModerationSettingsNotFoundException ex) {
                        callbackSender.showChatUnavailableCallback(callbackId, userLocale);
                    }
                    return ProcessingResult.create(processedUserState, messageId, chatId, filterType);
                },
                callbackData
        );
    }

    private void showFilterStateChangedCallback(String userLocale, String callbackId, boolean newState) {
        var callbackText = newState
                ? CallbackTextCode.FILTER_ENABLE
                : CallbackTextCode.FILTER_DISABLE;
        callbackSender.showAnswerCallback(
                CallbackAnswerPayload.create(callbackText),
                userLocale,
                callbackId
        );
    }
}
