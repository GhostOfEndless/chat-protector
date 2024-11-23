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
import ru.tbank.processor.service.personal.payload.MessageArgument;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;
import ru.tbank.processor.utils.TelegramUtils;

import java.util.List;

@NullMarked
@Slf4j
@Component
public final class ChatStateHandler extends PersonalUpdateHandler {

    private final GroupChatService groupChatService;

    public ChatStateHandler(
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService,
            GroupChatService groupChatService) {
        super(personalChatService, telegramClientService, textResourceService, UserState.CHAT);
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        long chatId = (Long) args[0];

        return groupChatService.findById(chatId)
                .map(chatRecord -> MessagePayload.create(
                        MessageTextCode.CHAT_MESSAGE,
                        List.of(
                                MessageArgument.createTextArgument(chatRecord.getName())
                        ),
                        List.of(
                                CallbackButtonPayload.create(ButtonTextCode.CHAT_BUTTON_FILTERS_SETTINGS, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.CHAT_BUTTON_EXCLUDE, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK)
                        )))
                .orElseGet(chatNotFoundMessage);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        int callbackMessageId = callbackQuery.getMessage().getMessageId();
        var callbackData = TelegramUtils.parseCallbackWithParams(callbackQuery.getData());
        var chatId = callbackData.chatId();

        return switch (callbackData.pressedButton()) {
            case BUTTON_BACK -> ProcessingResult.create(UserState.CHATS, callbackMessageId);
            case CHAT_BUTTON_FILTERS_SETTINGS -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> ProcessingResult.create(UserState.FILTERS, callbackMessageId, chatId),
                    callbackQuery
            );
            case CHAT_BUTTON_EXCLUDE -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> ProcessingResult.create(UserState.CHAT_DELETION, callbackMessageId, chatId),
                    callbackQuery
            );
            default -> ProcessingResult.create(UserState.START, callbackMessageId);
        };
    }
}
