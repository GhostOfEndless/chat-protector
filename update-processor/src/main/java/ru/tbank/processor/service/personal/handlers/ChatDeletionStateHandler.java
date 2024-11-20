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
public final class ChatDeletionStateHandler extends PersonalUpdateHandler {

    private final GroupChatService groupChatService;

    public ChatDeletionStateHandler(
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService,
            GroupChatService groupChatService
    ) {
        super(personalChatService, telegramClientService, textResourceService, UserState.CHAT_DELETION);
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(UserRole userRole, Object[] args) {
        long chatId = (Long) args[0];
        return groupChatService.findById(chatId)
                .map(chatRecord -> MessagePayload.create(
                        MessageTextCode.CHAT_DELETION_MESSAGE,
                        List.of(
                                CallbackButtonPayload.create(ButtonTextCode.CHAT_DELETION_BUTTON_CONFIRM, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK, chatId)
                        )))
                .orElseGet(chatNotFoundMessage);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        int callbackMessageId = callbackQuery.getMessage().getMessageId();
        var callbackData = TelegramUtils.parseCallbackWithParams(callbackQuery.getData());
        var chatId = callbackData.chatId();

        if (chatId == 0) {
            showChatUnavailableCallback(callbackQuery.getId(), userRecord.getLocale());
            return ProcessingResult.create(UserState.CHATS, callbackMessageId);
        }

        return switch (callbackData.pressedButton()) {
            case BUTTON_BACK -> new ProcessingResult(UserState.CHAT, callbackMessageId, new Object[]{chatId});
            case CHAT_DELETION_BUTTON_CONFIRM -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> {
                        log.debug("Chat with id={} deletion", chatId);
                        return new ProcessingResult(processedUserState, callbackMessageId, new Object[]{chatId});
                    },
                    callbackQuery
            );
            default -> ProcessingResult.create(UserState.START, callbackMessageId);
        };
    }
}
