package ru.tbank.processor.service.personal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.payload.CallbackAnswerPayload;
import ru.tbank.processor.service.personal.payload.CallbackArgument;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallbackAnswerSenderTest {

    @Mock
    private TelegramClientService telegramClientService;

    @Mock
    private TextResourceService textResourceService;

    @InjectMocks
    private CallbackAnswerSender callbackAnswerSender;

    private static final String USER_LOCALE = "en";
    private static final String CALLBACK_QUERY_ID = "callback_123";
    private static final String PROCESSED_TEXT = "Processed Callback Text";

    @BeforeEach
    void setUp() {
        lenient().when(textResourceService.getCallbackText(any(), any(), eq(USER_LOCALE)))
                .thenReturn(PROCESSED_TEXT);
        lenient().when(textResourceService.getText(anyString(), eq(USER_LOCALE)))
                .thenReturn("Translated Argument");
    }

    @Test
    void showAnswerCallback_withAlert_sendsCallbackWithAlert() {
        var payload = createCallbackAnswerPayload();
        boolean isAlert = true;

        callbackAnswerSender.showAnswerCallback(payload, USER_LOCALE, CALLBACK_QUERY_ID, isAlert);

        verify(telegramClientService).sendCallbackAnswer(PROCESSED_TEXT, CALLBACK_QUERY_ID, isAlert);
        verify(textResourceService).getCallbackText(any(), any(), eq(USER_LOCALE));
    }

    @Test
    void showAnswerCallback_withoutAlert_sendsCallbackWithoutAlert() {
        var payload = createCallbackAnswerPayload();

        callbackAnswerSender.showAnswerCallback(payload, USER_LOCALE, CALLBACK_QUERY_ID);

        verify(telegramClientService).sendCallbackAnswer(PROCESSED_TEXT, CALLBACK_QUERY_ID, false);
        verify(textResourceService).getCallbackText(any(), any(), eq(USER_LOCALE));
    }

    @Test
    void showAnswerCallback_withResourceArguments_processesArgumentsCorrectly() {
        var payload = createCallbackAnswerPayloadWithResourceArgs();

        callbackAnswerSender.showAnswerCallback(payload, USER_LOCALE, CALLBACK_QUERY_ID, true);

        verify(textResourceService).getText(eq("arg1"), eq(USER_LOCALE));
        verify(telegramClientService).sendCallbackAnswer(eq(PROCESSED_TEXT), eq(CALLBACK_QUERY_ID), eq(true));
    }

    @Test
    void showChatUnavailableCallback_sendsChatUnavailableMessage() {
        when(textResourceService.getCallbackText(eq(CallbackTextCode.CHAT_UNAVAILABLE), any(), eq(USER_LOCALE)))
                .thenReturn("Chat Unavailable");

        callbackAnswerSender.showChatUnavailableCallback(CALLBACK_QUERY_ID, USER_LOCALE);

        verify(telegramClientService).sendCallbackAnswer("Chat Unavailable", CALLBACK_QUERY_ID, false);
        verify(textResourceService).getCallbackText(any(), any(), eq(USER_LOCALE));
    }

    @Test
    void showMessageExpiredCallback_sendsMessageExpiredWithAlert() {
        when(textResourceService.getCallbackText(eq(CallbackTextCode.MESSAGE_EXPIRED), any(), eq(USER_LOCALE)))
                .thenReturn("Message Expired");

        callbackAnswerSender.showMessageExpiredCallback(USER_LOCALE, CALLBACK_QUERY_ID);

        verify(telegramClientService).sendCallbackAnswer("Message Expired", CALLBACK_QUERY_ID, true);
        verify(textResourceService).getCallbackText(eq(CallbackTextCode.MESSAGE_EXPIRED), any(), eq(USER_LOCALE));
    }

    @Test
    void showPermissionDeniedCallback_sendsPermissionDeniedWithAlert() {
        when(textResourceService.getCallbackText(eq(CallbackTextCode.PERMISSION_DENIED), any(), eq(USER_LOCALE)))
                .thenReturn("Permission Denied");

        callbackAnswerSender.showPermissionDeniedCallback(USER_LOCALE, CALLBACK_QUERY_ID);

        verify(telegramClientService).sendCallbackAnswer("Permission Denied", CALLBACK_QUERY_ID, true);
        verify(textResourceService).getCallbackText(eq(CallbackTextCode.PERMISSION_DENIED), any(), eq(USER_LOCALE));
    }

    private CallbackAnswerPayload createCallbackAnswerPayload() {
        CallbackAnswerPayload payload = mock(CallbackAnswerPayload.class);
        when(payload.callbackText()).thenReturn(CallbackTextCode.MESSAGE_EXPIRED);
        when(payload.callbackArgs()).thenReturn(List.of(mock(CallbackArgument.class)));
        when(payload.callbackArgs().getFirst().isResource()).thenReturn(false);
        when(payload.callbackArgs().getFirst().text()).thenReturn("arg1");
        return payload;
    }

    private CallbackAnswerPayload createCallbackAnswerPayloadWithResourceArgs() {
        CallbackAnswerPayload payload = mock(CallbackAnswerPayload.class);
        when(payload.callbackText()).thenReturn(CallbackTextCode.MESSAGE_EXPIRED);
        when(payload.callbackArgs()).thenReturn(List.of(mock(CallbackArgument.class)));
        when(payload.callbackArgs().getFirst().isResource()).thenReturn(true);
        when(payload.callbackArgs().getFirst().text()).thenReturn("arg1");
        return payload;
    }
}
