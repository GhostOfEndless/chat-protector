package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserRole;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;
import ru.tbank.processor.utils.TelegramUtils;

import java.util.List;

@NullMarked
@Slf4j
@Component
public final class TextFiltersStateHandler extends PersonalUpdateHandler {

    private final GroupChatService groupChatService;

    public TextFiltersStateHandler(
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService,
            GroupChatService groupChatService
    ) {
        super(personalChatService, telegramClientService, textResourceService, UserState.TEXT_FILTERS);
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(UserRole userRole, Object[] args) {
        long chatId = (Long) args[0];

        return groupChatService.findById(chatId)
                .map(chatRecord -> MessagePayload.create(
                        MessageTextCode.TEXT_FILTERS_MESSAGE,
                        List.of(
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_BUTTON_TAGS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_BUTTON_EMAILS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_BUTTON_LINKS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_BUTTON_MENTIONS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_BUTTON_BOT_COMMANDS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_BUTTON_CUSTOM_EMOJIS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.TEXT_FILTERS_BUTTON_PHONE_NUMBERS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK, chatId)
                        )
                ))
                .orElseGet(chatNotFoundMessage);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        int callbackMessageId = callbackQuery.getMessage().getMessageId();
        var callbackData = TelegramUtils.parseCallbackWithParams(callbackQuery.getData());
        var pressedButton = callbackData.pressedButton();
        long chatId = callbackData.chatId();

        if (chatId == 0) {
            // TODO: Добавить callback, что такого чата не существует
            return ProcessingResult.create(UserState.CHATS, callbackMessageId);
        }

        if (pressedButton.isButtonFilterType()) {
            var filterType = pressedButton.getFilterTypeFromButton();
            return checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> new ProcessingResult(
                            UserState.TEXT_FILTER,
                            callbackMessageId,
                            new Object[]{chatId, filterType}
                    ),
                    new Object[]{chatId},
                    callbackQuery
            );
        } else if (pressedButton == ButtonTextCode.BUTTON_BACK) {
            return new ProcessingResult(UserState.FILTERS, callbackMessageId, new Object[]{chatId});
        } else {
            return new ProcessingResult(processedUserState, callbackMessageId, new Object[]{chatId});
        }
    }
}
