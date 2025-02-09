package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
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

import java.util.List;

@NullMarked
@Slf4j
@Component
public final class ChatAdditionStateHandler extends PersonalUpdateHandler {

    private final TelegramClientService telegramClientService;

    public ChatAdditionStateHandler(
            PersonalChatService personalChatService,
            CallbackAnswerSender callbackSender,
            MessageSender messageSender,
            TelegramClientService telegramClientService
    ) {
        super(personalChatService, callbackSender, messageSender, UserState.CHAT_ADDITION);
        this.telegramClientService = telegramClientService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        return telegramClientService.getBotUserName()
                .map(this::buildChatAdditionMessage)
                .orElseGet(this::buildErrorMessage);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer messageId = callbackData.messageId();
        if (!callbackData.pressedButton().isBackButton()) {
            return ProcessingResult.create(processedUserState, messageId);
        }
        return ProcessingResult.create(UserState.CHATS, messageId);
    }

    private MessagePayload buildErrorMessage() {
        return MessagePayload.create(
                MessageTextCode.CHAT_ADDITION_ERROR_MESSAGE,
                List.of(
                        CallbackButtonPayload.create(ButtonTextCode.BACK)
                )
        );
    }

    private MessagePayload buildChatAdditionMessage(String botUserName) {
        String additionUrl = telegramClientService.createBotAdditionUrl(botUserName);
        return MessagePayload.create(
                MessageTextCode.CHAT_ADDITION_MESSAGE,
                List.of(
                        CallbackButtonPayload.createUrlButton(ButtonTextCode.CHAT_ADDITION_ADD, additionUrl),
                        CallbackButtonPayload.create(ButtonTextCode.BACK)
                )
        );
    }
}
