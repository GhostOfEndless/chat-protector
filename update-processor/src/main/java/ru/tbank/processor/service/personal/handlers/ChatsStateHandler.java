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

import java.util.stream.Collectors;

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
        var groupChatsButtons = groupChats.stream()
                .map(groupChatRecord -> new CallbackButtonPayload(
                        groupChatRecord.getName(),
                        String.valueOf(groupChatRecord.getId())
                ))
                .collect(Collectors.toList());

        return switch (userRole) {
            case ADMIN -> {
                groupChatsButtons.add(CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK));
                yield MessagePayload.builder()
                        .messageText(MessageTextCode.CHATS_MESSAGE_ADMIN)
                        .buttons(groupChatsButtons)
                        .build();
            }
            case OWNER -> {
                groupChatsButtons.add(CallbackButtonPayload.create(ButtonTextCode.CHATS_BUTTON_CHAT_ADDITION));
                groupChatsButtons.add(CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK));
                yield MessagePayload.builder()
                        .messageText(MessageTextCode.CHATS_MESSAGE_OWNER)
                        .buttons(groupChatsButtons)
                        .build();
            }
            default -> throw new IllegalStateException("Unexpected role: %s".formatted(userRole));
        };
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        super.processCallbackButtonUpdate(callbackQuery, userRecord);
        var callbackQueryId = callbackQuery.getId();
        var callbackMessageId = callbackQuery.getMessage().getMessageId();
        UserRole userRole = UserRole.valueOf(userRecord.getRole());

        if (NumberUtils.isParsable(callbackQuery.getData())) {
            if (UserRole.ADMIN.isEqualOrLowerThan(userRole)) {
                long chatId = Long.parseLong(callbackQuery.getData());
                return new ProcessingResult(UserState.CHAT, callbackMessageId, new Object[]{chatId});
            } else {
                showPermissionDeniedCallback(userRecord.getLocale(), callbackQueryId);
                return new ProcessingResult(processedUserState, callbackMessageId, new Object[]{});
            }
        } else {
            var pressedButton = ButtonTextCode.valueOf(callbackQuery.getData());
            return switch (pressedButton) {
                case BUTTON_BACK -> new ProcessingResult(UserState.START, callbackMessageId, new Object[]{});
                case CHATS_BUTTON_CHAT_ADDITION -> {
                    if (UserRole.OWNER.isEqualOrLowerThan(userRole)) {
                        yield new ProcessingResult(UserState.CHAT_ADDITION, callbackMessageId, new Object[]{});
                    } else {
                        showPermissionDeniedCallback(userRecord.getLocale(), callbackQueryId);
                        yield new ProcessingResult(processedUserState, callbackMessageId, new Object[]{});
                    }
                }
                default -> new ProcessingResult(processedUserState, callbackMessageId, new Object[]{});
            };
        }
    }
}
