package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;
import ru.tbank.processor.utils.TelegramUtils;

import java.util.List;

@NullMarked
@Slf4j
@Component
public final class ChatAdditionStateHandler extends PersonalUpdateHandler {

    public ChatAdditionStateHandler(
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService
    ) {
        super(personalChatService, telegramClientService, textResourceService, UserState.CHAT_ADDITION);
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        User bot = telegramClientService.getMe();

        if (bot.getUserName() == null) {
            return buildErrorMessage();
        }
        return buildChatAdditionMessage(bot);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        var callbackMessageId = callbackQuery.getMessage().getMessageId();
        String callbackData = callbackQuery.getData();
        var pressedButton = ButtonTextCode.valueOf(callbackData);

        if (!pressedButton.isBackButton()) {
            return ProcessingResult.create(processedUserState, callbackMessageId);
        }

        return ProcessingResult.create(UserState.CHATS, callbackMessageId);
    }

    private MessagePayload buildErrorMessage() {
        return MessagePayload.create(
                MessageTextCode.CHAT_ADDITION_ERROR_MESSAGE,
                List.of(
                        CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK)
                )
        );
    }

    private MessagePayload buildChatAdditionMessage(User bot) {
        String additionUrl = TelegramUtils.createBotAdditionUrl(bot.getUserName());
        return MessagePayload.create(
                MessageTextCode.CHAT_ADDITION_MESSAGE,
                List.of(
                        CallbackButtonPayload.create(ButtonTextCode.CHAT_ADDITION_BUTTON_ADD, additionUrl),
                        CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK)
                )
        );
    }
}
