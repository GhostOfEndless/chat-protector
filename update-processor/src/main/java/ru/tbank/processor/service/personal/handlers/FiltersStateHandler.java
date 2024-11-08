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

import java.util.List;

@NullMarked
@Slf4j
@Component
public final class FiltersStateHandler extends PersonalUpdateHandler {

    private final GroupChatService groupChatService;

    public FiltersStateHandler(
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService,
            GroupChatService groupChatService
    ) {
        super(personalChatService, telegramClientService, textResourceService, UserState.FILTERS);
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(UserRole userRole, Object[] args) {
        long chatId = (Long) args[0];

        return groupChatService.findById(chatId)
                .map(chatRecord -> new MessagePayload(
                        MessageTextCode.FILTERS_MESSAGE,
                        List.of(
                                CallbackButtonPayload.create(ButtonTextCode.FILTERS_BUTTON_TEXT_FILTERS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK, chatId)
                        ),
                        new String[]{}))
                .orElseGet(() -> new MessagePayload(
                        MessageTextCode.CHAT_MESSAGE_NOT_FOUND,
                        List.of(
                                CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK)
                        ),
                        new String[]{}
                ));
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        int callbackMessageId = callbackQuery.getMessage().getMessageId();
        String[] callbackData = callbackQuery.getData().split(":");

        var pressedButton = ButtonTextCode.valueOf(callbackData[0]);
        long chatId = callbackData.length == 2
                ? Long.parseLong(callbackData[1])
                : 0;

        if (chatId == 0) {
            return new ProcessingResult(UserState.CHATS, callbackMessageId, new Object[]{});
        }

        return switch (pressedButton) {
            case BUTTON_BACK -> new ProcessingResult(UserState.CHAT, callbackMessageId, new Object[]{chatId});
            case FILTERS_BUTTON_TEXT_FILTERS -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> new ProcessingResult(UserState.TEXT_FILTERS, callbackMessageId, new Object[]{chatId}),
                    new Object[]{chatId},
                    callbackQuery
            );
            default -> new ProcessingResult(processedUserState, callbackMessageId, new Object[]{chatId});
        };
    }
}
