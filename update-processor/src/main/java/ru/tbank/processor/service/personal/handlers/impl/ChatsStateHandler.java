package ru.tbank.processor.service.personal.handlers.impl;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserRole;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.handlers.PersonalUpdateHandler;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.utils.UpdateType;

import java.util.stream.Collectors;

@NullMarked
@Slf4j
@Component
public final class ChatsStateHandler extends PersonalUpdateHandler {

    private final GroupChatService groupChatService;

    public ChatsStateHandler(
            AppUserService appUserService,
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService,
            GroupChatService groupChatService) {
        super(appUserService, personalChatService, telegramClientService, textResourceService, UserState.CHATS);
        this.groupChatService = groupChatService;
    }

    @Override
    protected void processUpdate(UpdateType updateType, Update update, AppUserRecord userRecord) {
        if (updateType == UpdateType.CALLBACK) {

        }
    }

    @Override
    protected void goToState(AppUserRecord userRecord, Integer messageId) {

    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord) {
        UserRole userRole = UserRole.valueOf(userRecord.getRole()); // убрать role
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

                yield new MessagePayload(
                        MessageTextCode.CHATS_MESSAGE_ADMIN,
                        groupChatsButtons
                );
            }
            case OWNER -> {
                groupChatsButtons.add(CallbackButtonPayload.create(ButtonTextCode.CHATS_BUTTON_CHAT_ADDITION));
                groupChatsButtons.add(CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK));

                yield new MessagePayload(
                        MessageTextCode.CHATS_MESSAGE_OWNER,
                        groupChatsButtons
                );
            }
            default -> throw new IllegalStateException("Unexpected role: %s".formatted(userRole));
        };
    }

    private void processCallbackButton(CallbackQuery callbackQuery) {
        var callbackQueryId = callbackQuery.getId();
        var callbackMessageId = callbackQuery.getMessage().getMessageId();
    }
}
