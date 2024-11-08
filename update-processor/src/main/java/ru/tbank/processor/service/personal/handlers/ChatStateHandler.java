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
public class ChatStateHandler extends PersonalUpdateHandler {

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
    protected MessagePayload buildMessagePayloadForUser(UserRole userRole, Object[] args) {
        long chatId = (Long) args[0];
        var groupChatRecord = groupChatService.findById(chatId);

        if (groupChatRecord.isPresent()) {
            return MessagePayload.builder()
                    .messageText(MessageTextCode.CHAT_MESSAGE)
                    .messageArgs(new String[]{groupChatRecord.get().getName()})
                    .buttons(List.of(
                            CallbackButtonPayload.create(ButtonTextCode.CHAT_BUTTON_FILTERS_SETTINGS, chatId),
                            CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK)
                    ))
                    .build();
        } else {
            return MessagePayload.builder()
                    .messageText(MessageTextCode.CHAT_MESSAGE_NOT_FOUND)
                    .buttons(List.of(
                            CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK)
                    ))
                    .build();
        }
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        String callbackQueryId = callbackQuery.getId();
        int callbackMessageId = callbackQuery.getMessage().getMessageId();
        String[] callbackData = callbackQuery.getData().split(":");

        var pressedButton = ButtonTextCode.valueOf(callbackData[0]);
        long chatId = callbackData.length == 2 ? Long.parseLong(callbackData[1]) : 0;

        UserRole userRole = UserRole.valueOf(userRecord.getRole());

        if (chatId == 0 || pressedButton == ButtonTextCode.BUTTON_BACK) {
            return new ProcessingResult(UserState.CHATS, callbackMessageId, new Object[]{});
        } else if (pressedButton == ButtonTextCode.CHAT_BUTTON_FILTERS_SETTINGS) {
            if (UserRole.ADMIN.isEqualOrLowerThan(userRole)) {
                return new ProcessingResult(UserState.FILTERS, callbackMessageId, new Object[]{chatId});
            } else {
                showPermissionDeniedCallback(userRecord.getLocale(), callbackQueryId);
                return new ProcessingResult(processedUserState, callbackMessageId, new Object[]{chatId});
            }
        } else {
            return new ProcessingResult(processedUserState, callbackMessageId, new Object[]{chatId});
        }
    }
}
