package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.tbank.common.entity.FilterType;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
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

    private final GroupChatService groupChatService;

    public GenericTextFilterStateHandler(
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService,
            GroupChatService groupChatService
    ) {
        super(personalChatService, telegramClientService, textResourceService, UserState.TEXT_FILTER);
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(UserRole userRole, Object[] args) {
        long chatId = (Long) args[0];
        FilterType filterType = (FilterType) args[1];

        return groupChatService.findById(chatId)
                .map(chatRecord -> new MessagePayload(
                        MessageTextCode.TEXT_FILTER_MESSAGE,
                        List.of(
                                MessageArgument.createResourceArgument(MessageTextCode.getFilterNameByType(filterType))
                        ),
                        List.of(
                                CallbackButtonPayload.create(
                                        ButtonTextCode.TEXT_FILTER_BUTTON_ENABLE,
                                        chatId,
                                        filterType.name()
                                ),
                                CallbackButtonPayload.create(
                                        ButtonTextCode.TEXT_FILTER_BUTTON_DISABLE,
                                        chatId,
                                        filterType.name()
                                ),
                                CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK, chatId)
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
            return new ProcessingResult(UserState.CHATS, callbackMessageId, new Object[]{});
        } else if (filterTypeName.isEmpty() || pressedButton == ButtonTextCode.BUTTON_BACK) {
            return new ProcessingResult(UserState.TEXT_FILTERS, callbackMessageId, new Object[]{chatId});
        }

        var filterType = FilterType.valueOf(filterTypeName);
        return switch (pressedButton) {
            case TEXT_FILTER_BUTTON_ENABLE -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> {
                        log.debug("Filter type to enable is: {}", filterType.name());
                        showRegularCallback(CallbackTextCode.FILTER_ENABLE, userRecord.getLocale(), callbackQuery.getId());
                        return new ProcessingResult(processedUserState, callbackMessageId, new Object[]{chatId, filterType});
                    },
                    callbackQuery
            );
            case TEXT_FILTER_BUTTON_DISABLE -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> {
                        log.debug("Filter type to disable is: {}", filterType.name());
                        showRegularCallback(CallbackTextCode.FILTER_DISABLE, userRecord.getLocale(), callbackQuery.getId());
                        return new ProcessingResult(processedUserState, callbackMessageId, new Object[]{chatId, filterType});
                    },
                    callbackQuery
            );
            default -> ProcessingResult.create(UserState.START, callbackMessageId);
        };
    }
}
