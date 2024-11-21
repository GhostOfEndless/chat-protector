package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.tbank.common.entity.FilterType;
import ru.tbank.processor.excpetion.ChatModerationSettingsNotFoundException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.moderation.TextModerationSettingsService;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserRole;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.MessageArgument;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;
import ru.tbank.processor.utils.TelegramUtils;

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
            TelegramClientService telegramClientService,
            TextResourceService textResourceService,
            GroupChatService groupChatService
    ) {
        super(personalChatService, telegramClientService, textResourceService, UserState.TEXT_FILTER);
        this.textModerationSettingsService = textModerationSettingsService;
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(UserRole userRole, Object[] args) {
        long chatId = (Long) args[0];
        FilterType filterType = (FilterType) args[1];

        var filterSettings = textModerationSettingsService.getFilterSettings(chatId, filterType);
        var buttonText = filterSettings.isEnabled()
                ? ButtonTextCode.TEXT_FILTER_BUTTON_DISABLE
                : ButtonTextCode.TEXT_FILTER_BUTTON_ENABLE;
        return groupChatService.findById(chatId)
                .map(chatRecord -> new MessagePayload(
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
    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        int callbackMessageId = callbackQuery.getMessage().getMessageId();
        var callbackData = TelegramUtils.parseCallbackWithParams(callbackQuery.getData());
        var pressedButton = callbackData.pressedButton();
        String filterTypeName = callbackData.additionalData();
        long chatId = callbackData.chatId();

        if (chatId == 0) {
            showChatUnavailableCallback(callbackQuery.getId(), userRecord.getLocale());
            return ProcessingResult.create(UserState.CHATS, callbackMessageId);
        } else if (filterTypeName.isEmpty() || pressedButton == ButtonTextCode.BUTTON_BACK) {
            return new ProcessingResult(UserState.TEXT_FILTERS, callbackMessageId, new Object[]{chatId});
        } else if (!(pressedButton == ButtonTextCode.TEXT_FILTER_BUTTON_ENABLE
                || pressedButton == ButtonTextCode.TEXT_FILTER_BUTTON_DISABLE)) {
            return ProcessingResult.create(UserState.START, callbackMessageId);
        }

        var filterType = FilterType.valueOf(filterTypeName);
        return processFilterStateChanged(
                chatId,
                userRecord,
                filterType,
                pressedButton == ButtonTextCode.TEXT_FILTER_BUTTON_ENABLE,
                callbackQuery
        );
    }

    private ProcessingResult processFilterStateChanged(
            Long chatId,
            AppUserRecord userRecord,
            FilterType filterType,
            boolean newState,
            CallbackQuery callbackQuery
    ) {
        return checkPermissionAndProcess(
                UserRole.ADMIN,
                userRecord,
                () -> {
                    Integer messageId = callbackQuery.getMessage().getMessageId();
                    String callbackId = callbackQuery.getId();
                    Object[] args = new Object[]{chatId, filterType};

                    try {
                        log.debug("Filter type to enable is: {}", filterType.name());
                        textModerationSettingsService.updateFilterState(chatId, filterType, newState);
                        // TODO: сделать ожидание обновления в Redis с каким-то timeout,
                        //  если timeout истёк - отправить ошибку

                        var callbackText = newState
                                ? CallbackTextCode.FILTER_ENABLE
                                : CallbackTextCode.FILTER_DISABLE;
                        showAnswerCallback(callbackText, userRecord.getLocale(), callbackId, false);
                        goToState(userRecord, messageId, args);
                    } catch (ChatModerationSettingsNotFoundException ex) {
                        showAnswerCallback(
                                CallbackTextCode.CHAT_UNAVAILABLE,
                                userRecord.getLocale(),
                                callbackId,
                                false
                        );
                    }

                    return new ProcessingResult(processedUserState, messageId, args);
                },
                callbackQuery
        );
    }
}
