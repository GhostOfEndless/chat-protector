package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
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

@NullMarked
@Slf4j
@Component
public final class ChatsStateHandler extends PersonalUpdateHandler {

    private final GroupChatService groupChatService;

    public ChatsStateHandler(
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService,
            GroupChatService groupChatService
    ) {
        super(personalChatService, telegramClientService, textResourceService, UserState.CHATS);
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(UserRole userRole, Object[] args) {
        var groupChats = groupChatService.findAll();
        var groupChatsButtons = TelegramUtils.buildChatButtons(groupChats);

        return switch (userRole) {
            case ADMIN -> {
                groupChatsButtons.add(CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK));
                yield new MessagePayload(MessageTextCode.CHATS_MESSAGE_ADMIN, groupChatsButtons, new String[]{});
            }
            case OWNER -> {
                groupChatsButtons.add(CallbackButtonPayload.create(ButtonTextCode.CHATS_BUTTON_CHAT_ADDITION));
                groupChatsButtons.add(CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK));
                yield new MessagePayload(MessageTextCode.CHATS_MESSAGE_OWNER, groupChatsButtons, new String[]{});
            }
            default -> throw new IllegalStateException("Unexpected role: %s".formatted(userRole));
        };
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        var callbackMessageId = callbackQuery.getMessage().getMessageId();
        String callbackData = callbackQuery.getData();

        if (NumberUtils.isParsable(callbackQuery.getData())) {
            return checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> {
                        long chatId = Long.parseLong(callbackData);
                        return new ProcessingResult(UserState.CHAT, callbackMessageId, new Object[]{chatId});
                    },
                    new Object[]{},
                    callbackQuery
            );
        } else {
            var pressedButton = ButtonTextCode.valueOf(callbackData);
            return switch (pressedButton) {
                case BUTTON_BACK -> new ProcessingResult(UserState.START, callbackMessageId, new Object[]{});
                case CHATS_BUTTON_CHAT_ADDITION -> checkPermissionAndProcess(
                        UserRole.OWNER,
                        userRecord,
                        () -> new ProcessingResult(UserState.CHAT_ADDITION, callbackMessageId, new Object[]{}),
                        new Object[]{},
                        callbackQuery
                );
                default -> new ProcessingResult(processedUserState, callbackMessageId, new Object[]{});
            };
        }
    }
}
