package ru.tbank.processor.service.personal.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatAdditionStateHandlerTest {

    private TelegramClientService telegramClientService;
    private ChatAdditionStateHandler handler;

    @BeforeEach
    void setUp() {
        PersonalChatService personalChatService = mock(PersonalChatService.class);
        CallbackAnswerSender callbackSender = mock(CallbackAnswerSender.class);
        MessageSender messageSender = mock(MessageSender.class);
        telegramClientService = mock(TelegramClientService.class);

        handler = new ChatAdditionStateHandler(
                personalChatService,
                callbackSender,
                messageSender,
                telegramClientService
        );
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnChatAdditionMessage_whenBotUserNamePresent() {
        String botUserName = "MyTestBot";
        String expectedUrl = "https://t.me/MyTestBot?startgroup=start";

        when(telegramClientService.getBotUserName()).thenReturn(Optional.of(botUserName));
        when(telegramClientService.createBotAdditionUrl(botUserName)).thenReturn(expectedUrl);

        AppUserRecord user = new AppUserRecord();
        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.CHAT_ADDITION_MESSAGE);
        assertThat(payload.buttons()).hasSize(2);
        assertThat(payload.buttons().getFirst().code()).isEqualTo(expectedUrl);
        assertThat(payload.buttons().get(1).code()).isEqualTo(ButtonTextCode.BACK.name());
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnErrorMessage_whenBotUserNameMissing() {
        when(telegramClientService.getBotUserName()).thenReturn(Optional.empty());

        AppUserRecord user = new AppUserRecord();
        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.CHAT_ADDITION_ERROR_MESSAGE);
        assertThat(payload.buttons())
                .extracting(CallbackButtonPayload::code)
                .containsExactly(ButtonTextCode.BACK.name());
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnCurrentState_whenButtonIsNotBack() {
        CallbackData callbackData = new CallbackData(
                123,
                "callback_id",
                ButtonTextCode.CHAT_ADDITION_ADD,
                new String[]{}
        );

        AppUserRecord user = new AppUserRecord();
        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.CHAT_ADDITION);
        assertThat(result.messageId()).isEqualTo(123);
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnChatsState_whenPressedBack() {
        CallbackData callbackData = new CallbackData(
                456,
                "callback_id",
                ButtonTextCode.BACK,
                new String[]{}
        );

        AppUserRecord user = new AppUserRecord();
        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.CHATS);
        assertThat(result.messageId()).isEqualTo(456);
    }
}

