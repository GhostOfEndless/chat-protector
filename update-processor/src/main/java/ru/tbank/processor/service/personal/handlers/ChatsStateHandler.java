package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.CallbackAnswerSender;
import ru.tbank.processor.service.personal.MessageSender;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
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
            GroupChatService groupChatService,
            CallbackAnswerSender callbackSender,
            MessageSender messageSender
    ) {
        super(personalChatService, callbackSender, messageSender, UserState.CHATS);
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        var groupChats = groupChatService.findAll();
        var groupChatsButtons = TelegramUtils.buildChatButtons(groupChats);
        groupChatsButtons.add(CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK));
        UserRole userRole = UserRole.getRoleByName(userRecord.getRole());

        if (userRole != UserRole.OWNER) {
            return MessagePayload.create(MessageTextCode.CHATS_MESSAGE_ADMIN, groupChatsButtons);
        }

        groupChatsButtons.add(groupChatsButtons.size() - 1,
                CallbackButtonPayload.create(ButtonTextCode.CHATS_BUTTON_CHAT_ADDITION));
        return MessagePayload.create(MessageTextCode.CHATS_MESSAGE_OWNER, groupChatsButtons);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer messageId = callbackData.messageId();

        return switch (callbackData.pressedButton()) {
            case BUTTON_BACK -> ProcessingResult.create(UserState.START, messageId);
            case CHATS_BUTTON_CHAT_ADDITION -> checkPermissionAndProcess(
                    UserRole.OWNER,
                    userRecord,
                    () -> ProcessingResult.create(UserState.CHAT_ADDITION, messageId),
                    callbackData
            );
            case CHATS_BUTTON_CHAT -> checkPermissionAndProcess(
                    UserRole.ADMIN,
                    userRecord,
                    () -> ProcessingResult.create(UserState.CHAT, messageId, callbackData.getChatId()),
                    callbackData
            );
            default -> ProcessingResult.create(processedUserState, messageId);
        };
    }
}
