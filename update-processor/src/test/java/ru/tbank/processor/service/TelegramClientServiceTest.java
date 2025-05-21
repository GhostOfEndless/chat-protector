package ru.tbank.processor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.stickers.GetCustomEmojiStickers;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.tbank.processor.config.TelegramProperties;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramClientServiceTest {

    @Mock
    private TelegramProperties telegramProperties;

    @Mock
    private TelegramClient telegramClient;

    @InjectMocks
    private TelegramClientService telegramClientService;

    private static final Long CHAT_ID = 12345L;
    private static final Integer MESSAGE_ID = 100;
    private static final String MESSAGE_TEXT = "Test message";
    private static final String CALLBACK_QUERY_ID = "callback_123";
    private static final String CUSTOM_EMOJI_ID = "emoji_123";
    private static final String BOT_USERNAME = "@TestBot";
    private static final String BOT_ADDITION_URL = "https://t.me/%s";

    @BeforeEach
    void setUp() {
        lenient().when(telegramProperties.botAdditionUrl()).thenReturn(BOT_ADDITION_URL);
    }

    @Test
    void getEmojiPack_apiException_returnsEmptyList() throws TelegramApiException {
        when(telegramClient.execute(any(GetCustomEmojiStickers.class)))
                .thenThrow(new TelegramApiException("API Error"));

        List<Sticker> result = telegramClientService.getEmojiPack(CUSTOM_EMOJI_ID);

        assertEquals(Collections.emptyList(), result);
        verify(telegramClient).execute(any(GetCustomEmojiStickers.class));
    }

    @Test
    void deleteMessage_successfulExecution_executesDelete() throws TelegramApiException {
        when(telegramClient.execute(any(DeleteMessage.class))).thenReturn(true);

        telegramClientService.deleteMessage(CHAT_ID, MESSAGE_ID);

        verify(telegramClient).execute(any(DeleteMessage.class));
    }

    @Test
    void deleteMessage_apiException_logsError() throws TelegramApiException {
        when(telegramClient.execute(any(DeleteMessage.class)))
                .thenThrow(new TelegramApiException("API Error"));

        telegramClientService.deleteMessage(CHAT_ID, MESSAGE_ID);

        verify(telegramClient).execute(any(DeleteMessage.class));
    }

    @Test
    void sendMessage_successfulExecution_returnsMessage() throws TelegramApiException {
        Message message = new Message();
        message.setMessageId(MESSAGE_ID);
        InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup(List.of());
        when(telegramClient.execute(any(SendMessage.class))).thenReturn(message);

        Message result = telegramClientService.sendMessage(CHAT_ID, MESSAGE_TEXT, replyMarkup);

        assertEquals(message, result);
        verify(telegramClient).execute(any(SendMessage.class));
    }

    @Test
    void sendMessage_apiException_throwsException() throws TelegramApiException {
        when(telegramClient.execute(any(SendMessage.class)))
                .thenThrow(new TelegramApiException("API Error"));

        assertThrows(TelegramApiException.class, () ->
                telegramClientService.sendMessage(CHAT_ID, MESSAGE_TEXT, null));
        verify(telegramClient).execute(any(SendMessage.class));
    }

    @Test
    void editMessage_successfulExecution_executesEdit() throws TelegramApiException {
        InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup(List.of());
        when(telegramClient.execute(any(EditMessageText.class))).thenReturn(new Message());

        telegramClientService.editMessage(CHAT_ID, MESSAGE_ID, MESSAGE_TEXT, replyMarkup);

        verify(telegramClient).execute(any(EditMessageText.class));
    }

    @Test
    void editMessage_apiException_throwsException() throws TelegramApiException {
        when(telegramClient.execute(any(EditMessageText.class)))
                .thenThrow(new TelegramApiException("API Error"));

        assertThrows(TelegramApiException.class, () ->
                telegramClientService.editMessage(CHAT_ID, MESSAGE_ID, MESSAGE_TEXT, null));
        verify(telegramClient).execute(any(EditMessageText.class));
    }

    @Test
    void sendCallbackAnswer_successfulExecution_executesAnswer() throws TelegramApiException {
        when(telegramClient.execute(any(AnswerCallbackQuery.class))).thenReturn(true);

        telegramClientService.sendCallbackAnswer(MESSAGE_TEXT, CALLBACK_QUERY_ID, true);

        verify(telegramClient).execute(argThat((AnswerCallbackQuery method) ->
                method.getCallbackQueryId().equals(CALLBACK_QUERY_ID) &&
                        method.getText().equals(MESSAGE_TEXT) &&
                        method.getShowAlert()));
    }

    @Test
    void getBotUserName_successfulExecution_returnsUsername() throws TelegramApiException {
        User user = new User(0L, "fisrtName", false);
        user.setUserName(BOT_USERNAME);
        when(telegramClient.execute(any(GetMe.class))).thenReturn(user);

        Optional<String> result = telegramClientService.getBotUserName();

        assertTrue(result.isPresent());
        assertEquals(BOT_USERNAME, result.get());
        verify(telegramClient).execute(any(GetMe.class));
    }

    @Test
    void getBotUserName_apiException_returnsEmpty() throws TelegramApiException {
        when(telegramClient.execute(any(GetMe.class)))
                .thenThrow(new TelegramApiException("API Error"));

        Optional<String> result = telegramClientService.getBotUserName();

        assertTrue(result.isEmpty());
        verify(telegramClient).execute(any(GetMe.class));
    }

    @Test
    void leaveFromChat_successfulExecution_executesLeave() throws TelegramApiException {
        when(telegramClient.execute(any(LeaveChat.class))).thenReturn(true);

        telegramClientService.leaveFromChat(CHAT_ID);

        verify(telegramClient).execute(argThat((LeaveChat method) ->
                method.getChatId().equals(CHAT_ID.toString())));
    }

    @Test
    void leaveFromChat_apiException_logsError() throws TelegramApiException {
        when(telegramClient.execute(any(LeaveChat.class)))
                .thenThrow(new TelegramApiException("API Error"));

        telegramClientService.leaveFromChat(CHAT_ID);

        verify(telegramClient).execute(any(LeaveChat.class));
    }

    @Test
    void createBotAdditionUrl_formatsUrlCorrectly() {
        String expectedUrl = BOT_ADDITION_URL.formatted(BOT_USERNAME);

        String result = telegramClientService.createBotAdditionUrl(BOT_USERNAME);

        assertEquals(expectedUrl, result);
        verify(telegramProperties).botAdditionUrl();
    }
}
