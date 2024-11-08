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
public class FiltersStateHandler extends PersonalUpdateHandler {

    private final GroupChatService groupChatService;

    public FiltersStateHandler(
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService,
            GroupChatService groupChatService) {
        super(personalChatService, telegramClientService, textResourceService, UserState.FILTERS);
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(UserRole userRole, Object[] args) {
        long chatId = (Long) args[0];
        var groupChatRecord = groupChatService.findById(chatId);

        if (groupChatRecord.isPresent()) {
            return MessagePayload.builder()
                    .messageText(MessageTextCode.FILTERS_MESSAGE)
                    .buttons(List.of(
                            CallbackButtonPayload.create(ButtonTextCode.FILTERS_BUTTON_TEXT_FILTERS, chatId),
                            CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK, chatId)
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

        if (chatId == 0) {
            return new ProcessingResult(UserState.CHATS, callbackMessageId, new Object[]{});
        }

        return switch (pressedButton) {
            case BUTTON_BACK -> new ProcessingResult(UserState.CHAT, callbackMessageId, new Object[]{chatId});
            case FILTERS_BUTTON_TEXT_FILTERS -> {
                if (UserRole.ADMIN.isEqualOrLowerThan(userRole)) {
                    yield new ProcessingResult(UserState.TEXT_FILTERS, callbackMessageId, new Object[]{chatId});
                } else {
                    showPermissionDeniedCallback(userRecord.getLocale(), callbackQueryId);
                    yield new ProcessingResult(processedUserState, callbackMessageId, new Object[]{chatId});
                }
            }
            default -> new ProcessingResult(processedUserState, callbackMessageId, new Object[]{chatId});
        };
    }
}
