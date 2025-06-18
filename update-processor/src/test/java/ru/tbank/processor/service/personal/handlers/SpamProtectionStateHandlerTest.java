package ru.tbank.processor.service.personal.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.common.entity.spam.SpamProtectionSettings;
import ru.tbank.processor.excpetion.ChatModerationSettingsNotFoundException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.generated.tables.records.GroupChatRecord;
import ru.tbank.processor.service.moderation.SpamProtectionSettingsService;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.CallbackAnswerSender;
import ru.tbank.processor.service.personal.MessageSender;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpamProtectionStateHandlerTest {

    private SpamProtectionSettingsService spamProtectionSettingsService;
    private CallbackAnswerSender callbackSender;
    private GroupChatService groupChatService;

    private SpamProtectionStateHandler handler;

    @BeforeEach
    void setUp() {
        MessageSender messageSender = mock(MessageSender.class);
        PersonalChatService personalChatService = mock(PersonalChatService.class);
        callbackSender = mock(CallbackAnswerSender.class);
        groupChatService = mock(GroupChatService.class);
        spamProtectionSettingsService = mock(SpamProtectionSettingsService.class);

        handler = new SpamProtectionStateHandler(
                spamProtectionSettingsService,
                personalChatService,
                callbackSender,
                groupChatService,
                messageSender
        );
    }

    @Test
    void buildMessagePayloadForUser_whenChatFoundAndProtectionEnabled_shouldReturnEnableButton() {
        long chatId = 1L;
        SpamProtectionSettings settings = new SpamProtectionSettings();
        settings.setEnabled(true);
        settings.setCoolDownPeriod(10L);
        GroupChatRecord chat = new GroupChatRecord();
        chat.setId(chatId);
        chat.setName("TestChat");

        when(spamProtectionSettingsService.getSettings(chatId)).thenReturn(settings);
        when(groupChatService.findById(chatId)).thenReturn(Optional.of(chat));

        AppUserRecord user = new AppUserRecord();

        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{chatId});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.SPAM_PROTECTION_MESSAGE);
        assertThat(payload.messageArgs()).anyMatch(arg -> arg.text().equals("10"));
        assertThat(payload.buttons())
                .extracting("code")
                .containsExactly(
                        ButtonTextCode.SPAM_PROTECTION_DISABLE.name() + ":1",
                        ButtonTextCode.BACK.name() + ":1"
                );
    }

    @Test
    void buildMessagePayloadForUser_whenChatFoundAndProtectionDisabled_shouldReturnDisableButton() {
        long chatId = 2L;
        SpamProtectionSettings settings = new SpamProtectionSettings();
        settings.setCoolDownPeriod(20L);
        GroupChatRecord chat = new GroupChatRecord();
        chat.setId(chatId);
        chat.setName("AnotherChat");

        when(spamProtectionSettingsService.getSettings(chatId)).thenReturn(settings);
        when(groupChatService.findById(chatId)).thenReturn(Optional.of(chat));

        AppUserRecord user = new AppUserRecord();

        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{chatId});

        assertThat(payload.buttons())
                .extracting("code")
                .containsExactly(
                        ButtonTextCode.SPAM_PROTECTION_ENABLE.name() + ":2",
                        ButtonTextCode.BACK + ":2"
                );
    }

    @Test
    void buildMessagePayloadForUser_whenChatNotFound_shouldReturnChatNotFoundMessage() {
        long chatId = 999L;

        when(groupChatService.findById(chatId)).thenReturn(Optional.empty());
        when(spamProtectionSettingsService.getSettings(eq(999L))).thenReturn(new SpamProtectionSettings());

        AppUserRecord user = new AppUserRecord();

        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{chatId});

        assertThat(payload).isEqualTo(handler.chatNotFoundMessage.get());
    }

    @Test
    void processCallbackButtonUpdate_whenChatIdZero_shouldShowUnavailableAndReturnChats() {
        int messageId = 5;
        String callbackId = "callbackId123";
        String locale = "en";

        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(0L);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.callbackId()).thenReturn(callbackId);

        AppUserRecord user = new AppUserRecord();
        user.setLocale(locale);

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        verify(callbackSender).showChatUnavailableCallback(callbackId, locale);
        assertThat(result.newState()).isEqualTo(UserState.CHATS);
        assertThat(result.messageId()).isEqualTo(messageId);
    }

    @Test
    void processCallbackButtonUpdate_whenBackButtonPressed_shouldReturnChatState() {
        int messageId = 6;
        long chatId = 10L;

        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(chatId);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.pressedButton()).thenReturn(ButtonTextCode.BACK);

        AppUserRecord user = new AppUserRecord();

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.CHAT);
        assertThat(result.messageId()).isEqualTo(messageId);
        assertThat(result.args()).containsExactly(chatId);
    }

    @Test
    void processCallbackButtonUpdate_whenUnknownButton_shouldReturnStartState() {
        int messageId = 7;
        long chatId = 20L;

        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(chatId);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.pressedButton()).thenReturn(ButtonTextCode.START_LANGUAGE);

        AppUserRecord user = new AppUserRecord();

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.START);
        assertThat(result.messageId()).isEqualTo(messageId);
    }

    @Test
    void processCallbackButtonUpdate_whenSpamProtectionEnableButton_andUserIsAdmin_shouldUpdateSettingsAndShowCallback() throws Exception {
        int messageId = 8;
        long chatId = 15L;
        String callbackId = "cbId";
        String locale = "ru";

        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(chatId);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.callbackId()).thenReturn(callbackId);
        when(callbackData.pressedButton()).thenReturn(ButtonTextCode.SPAM_PROTECTION_ENABLE);
        when(spamProtectionSettingsService.getSettings(eq(15L))).thenReturn(new SpamProtectionSettings());

        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.ADMIN.name());
        user.setLocale(locale);

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        verify(spamProtectionSettingsService).updateSettings(chatId, true);
        verify(callbackSender).showAnswerCallback(
                argThat(payload -> payload.callbackText() == CallbackTextCode.PROTECTION_ENABLE),
                eq(locale),
                eq(callbackId)
        );

        assertThat(result.newState()).isEqualTo(UserState.SPAM_PROTECTION);
        assertThat(result.messageId()).isEqualTo(messageId);
        assertThat(result.args()).containsExactly(chatId);
    }

    @Test
    void processCallbackButtonUpdate_whenSpamProtectionDisableButton_andChatSettingsNotFound_shouldShowUnavailableCallback() throws Exception {
        int messageId = 9;
        long chatId = 30L;
        String callbackId = "cbId2";
        String locale = "fr";

        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(chatId);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.callbackId()).thenReturn(callbackId);
        when(callbackData.pressedButton()).thenReturn(ButtonTextCode.SPAM_PROTECTION_DISABLE);

        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.ADMIN.name());
        user.setLocale(locale);

        doThrow(new ChatModerationSettingsNotFoundException("")).when(spamProtectionSettingsService).updateSettings(chatId, false);

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        verify(callbackSender).showChatUnavailableCallback(callbackId, locale);

        assertThat(result.newState()).isEqualTo(UserState.SPAM_PROTECTION);
        assertThat(result.messageId()).isEqualTo(messageId);
        assertThat(result.args()).containsExactly(chatId);
    }
}
