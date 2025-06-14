package ru.tbank.processor.service.personal.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.CallbackAnswerSender;
import ru.tbank.processor.service.personal.MessageSender;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.Language;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LanguageStateHandlerTest {

    private PersonalChatService personalChatService;
    private AppUserService appUserService;
    private CallbackAnswerSender callbackSender;
    private MessageSender messageSender;
    private LanguageStateHandler handler;

    @BeforeEach
    void setUp() {
        personalChatService = mock(PersonalChatService.class);
        appUserService = mock(AppUserService.class);
        callbackSender = mock(CallbackAnswerSender.class);
        messageSender = mock(MessageSender.class);

        handler = new LanguageStateHandler(
                personalChatService,
                appUserService,
                callbackSender,
                messageSender
        );
    }

    @Test
    void buildMessagePayloadForUser_shouldContainAllLanguagesAndBackButton() {
        AppUserRecord user = new AppUserRecord();
        user.setLocale(Language.ENGLISH.getLanguageCode());

        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.LANGUAGE_MESSAGE);
        assertThat(payload.messageArgs()).hasSize(1);
        assertThat(payload.buttons()).hasSize(Language.values().length + 1);
        assertThat(payload.buttons())
                .extracting(CallbackButtonPayload::code)
                .contains(ButtonTextCode.BACK.name());
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnStartState_whenPressedBack() {
        CallbackData callbackData = new CallbackData(
                10,
                "callbackId",
                ButtonTextCode.BACK,
                new String[]{}
        );
        AppUserRecord user = new AppUserRecord();

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.START);
        assertThat(result.messageId()).isEqualTo(10);
    }

    @Test
    void processCallbackButtonUpdate_shouldIgnoreNonLanguageButtons() {
        CallbackData callbackData = new CallbackData(
                20,
                "callbackId",
                ButtonTextCode.ACCOUNT_CHANGE_PASSWORD,
                new String[]{}
        );
        AppUserRecord user = new AppUserRecord();

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.LANGUAGE);
        assertThat(result.messageId()).isEqualTo(20);
    }

    @Test
    void processCallbackButtonUpdate_shouldShowLanguageNotChanged_whenSameLanguageSelected() {
        CallbackData callbackData = new CallbackData(
                30,
                "cbId",
                ButtonTextCode.LANGUAGE_ENGLISH,
                new String[]{}
        );
        AppUserRecord user = new AppUserRecord();
        user.setId(1L);
        user.setLocale(Language.ENGLISH.getLanguageCode());

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.LANGUAGE);
        assertThat(result.messageId()).isEqualTo(30);

        verify(callbackSender).showAnswerCallback(
                argThat(payload -> payload.callbackText() == CallbackTextCode.LANGUAGE_NOT_CHANGED),
                eq(Language.ENGLISH.getLanguageCode()),
                eq("cbId")
        );
        verifyNoInteractions(appUserService);
    }

    @Test
    void processCallbackButtonUpdate_shouldChangeLanguageAndGoToSameState_whenDifferentLanguageSelected() {
        CallbackData callbackData = new CallbackData(
                40,
                "cbId",
                ButtonTextCode.LANGUAGE_RUSSIAN,
                new String[]{}
        );
        AppUserRecord user = new AppUserRecord();
        user.setId(123L);
        user.setLocale(Language.ENGLISH.getLanguageCode());

        when(messageSender.updateUserMessage(anyLong(), anyString(), anyInt(), any())).thenReturn(999);

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.LANGUAGE);
        assertThat(result.messageId()).isEqualTo(40);

        verify(appUserService).updateLocale(123L, Language.RUSSIAN.getLanguageCode());
        verify(callbackSender).showAnswerCallback(
                argThat(payload -> payload.callbackText() == CallbackTextCode.LANGUAGE_CHANGED),
                eq(Language.ENGLISH.getLanguageCode()),
                eq("cbId")
        );
        verify(personalChatService).save(eq(123L), eq(UserState.LANGUAGE.name()), eq(999));
    }
}
