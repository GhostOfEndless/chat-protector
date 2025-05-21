package ru.tbank.processor.service.personal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tbank.processor.excpetion.StateNotUpdatedException;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.MessageArgument;
import ru.tbank.processor.service.personal.payload.MessagePayload;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageSenderTest {

    @Mock
    private TextResourceService textResourceService;

    @Mock
    private TelegramClientService telegramClientService;

    @InjectMocks
    private MessageSender messageSender;

    private static final Long USER_ID = 12345L;
    private static final String USER_LOCALE = "en";
    private static final Integer MESSAGE_ID = 100;
    private static final String BUTTON_TEXT = "button.text";
    private static final String BUTTON_CODE = "button_code";

    @BeforeEach
    void setUp() {
        when(textResourceService.getText(eq(BUTTON_TEXT), eq(USER_LOCALE)))
                .thenReturn("button");
        when(textResourceService.getMessageText(any(), any(), eq(USER_LOCALE)))
                .thenReturn("text");
    }

    @Test
    void updateUserMessage_newMessage_sendsMessageAndReturnsId() throws TelegramApiException {
        MessagePayload payload = createMessagePayload(false);
        Message message = new Message();
        message.setMessageId(MESSAGE_ID);
        when(telegramClientService.sendMessage(eq(USER_ID), anyString(), any(InlineKeyboardMarkup.class)))
                .thenReturn(message);

        var result = messageSender.updateUserMessage(USER_ID, USER_LOCALE, 0, payload);

        assertEquals(MESSAGE_ID, result);
        verify(telegramClientService).sendMessage(eq(USER_ID), eq("text"), any(InlineKeyboardMarkup.class));
        verify(telegramClientService, never()).editMessage(anyLong(), anyInt(), anyString(), any());
    }

    @Test
    void updateUserMessage_existingMessage_editsMessageAndReturnsId() throws TelegramApiException {
        MessagePayload payload = createMessagePayload(false);
        doNothing().when(telegramClientService).editMessage(eq(USER_ID), eq(MESSAGE_ID), anyString(), any(InlineKeyboardMarkup.class));

        var result = messageSender.updateUserMessage(USER_ID, USER_LOCALE, MESSAGE_ID, payload);

        assertEquals(MESSAGE_ID, result);
        verify(telegramClientService).editMessage(eq(USER_ID), eq(MESSAGE_ID), eq("text"), any(InlineKeyboardMarkup.class));
        verify(telegramClientService, never()).sendMessage(anyLong(), anyString(), any());
    }

    @Test
    void updateUserMessage_apiException_throwsStateNotUpdatedException() throws TelegramApiException {
        MessagePayload payload = createMessagePayload(false);
        when(telegramClientService.sendMessage(eq(USER_ID), anyString(), any(InlineKeyboardMarkup.class)))
                .thenThrow(new TelegramApiException("API Error"));

        assertThrows(StateNotUpdatedException.class, () ->
                messageSender.updateUserMessage(USER_ID, USER_LOCALE, 0, payload));
        verify(telegramClientService).sendMessage(eq(USER_ID), anyString(), any(InlineKeyboardMarkup.class));
    }

    @Test
    void updateUserMessage_withUrlButton_buildsKeyboardWithUrl() throws TelegramApiException {
        MessagePayload payload = createMessagePayload(true);
        Message message = new Message();
        message.setMessageId(MESSAGE_ID);
        when(telegramClientService.sendMessage(eq(USER_ID), anyString(), any(InlineKeyboardMarkup.class)))
                .thenReturn(message);

        messageSender.updateUserMessage(USER_ID, USER_LOCALE, 0, payload);

        verify(telegramClientService).sendMessage(eq(USER_ID), anyString(), argThat((InlineKeyboardMarkup markup) -> {
            var button = markup.getKeyboard().getFirst().getFirst();
            return button.getUrl() != null && button.getUrl().equals(BUTTON_CODE);
        }));
    }

    @Test
    void updateUserMessage_withCallbackButton_buildsKeyboardWithCallbackData() throws TelegramApiException {
        MessagePayload payload = createMessagePayload(false);
        Message message = new Message();
        message.setMessageId(MESSAGE_ID);
        when(telegramClientService.sendMessage(eq(USER_ID), anyString(), any(InlineKeyboardMarkup.class)))
                .thenReturn(message);

        messageSender.updateUserMessage(USER_ID, USER_LOCALE, 0, payload);

        verify(telegramClientService).sendMessage(eq(USER_ID), anyString(), argThat((InlineKeyboardMarkup markup) -> {
            var button = markup.getKeyboard().getFirst().getFirst();
            return button.getCallbackData() != null && button.getCallbackData().equals(BUTTON_CODE);
        }));
    }

    private MessagePayload createMessagePayload(boolean isUrl) {
        var button = mock(CallbackButtonPayload.class);
        when(button.text()).thenReturn(BUTTON_TEXT);
        when(button.code()).thenReturn(BUTTON_CODE);
        when(button.isUrl()).thenReturn(isUrl);

        var payload = mock(MessagePayload.class);
        when(payload.messageText()).thenReturn(MessageTextCode.ACCOUNT_MESSAGE);
        when(payload.buttons()).thenReturn(List.of(button));
        when(payload.messageArgs()).thenReturn(List.of(mock(MessageArgument.class)));
        when(payload.messageArgs().getFirst().isResource()).thenReturn(false);
        when(payload.messageArgs().getFirst().text()).thenReturn("arg1");
        return payload;
    }
}
