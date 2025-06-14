package ru.tbank.processor.service.personal.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tbank.common.entity.enums.FilterType;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.processor.excpetion.ChatModerationSettingsNotFoundException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.generated.tables.records.GroupChatRecord;
import ru.tbank.processor.service.moderation.TextModerationSettingsService;
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

class GenericTextFilterStateHandlerTest {

    private TextModerationSettingsService textModerationSettingsService;
    private GroupChatService groupChatService;
    private CallbackAnswerSender callbackSender;

    private GenericTextFilterStateHandler handler;

    @BeforeEach
    void setUp() {
        PersonalChatService personalChatService = mock(PersonalChatService.class);
        MessageSender messageSender = mock(MessageSender.class);
        textModerationSettingsService = mock(TextModerationSettingsService.class);
        groupChatService = mock(GroupChatService.class);
        callbackSender = mock(CallbackAnswerSender.class);

        handler = new GenericTextFilterStateHandler(
                textModerationSettingsService,
                personalChatService,
                groupChatService,
                callbackSender,
                messageSender
        );
    }

    @Test
    void buildMessagePayloadForUser_whenChatFoundAndFilterEnabled_shouldReturnDisableButton() {
        long chatId = 1L;
        FilterType filterType = FilterType.EMAILS;
        TextFilterSettings filterSettings = new TextFilterSettings();
        filterSettings.setEnabled(true);

        GroupChatRecord chatRecord = new GroupChatRecord();
        chatRecord.setId(chatId);
        chatRecord.setName("chatName");

        when(textModerationSettingsService.getFilterSettings(chatId, filterType)).thenReturn(filterSettings);
        when(groupChatService.findById(chatId)).thenReturn(Optional.of(chatRecord));

        AppUserRecord user = new AppUserRecord();

        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{chatId, filterType});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.TEXT_FILTER_MESSAGE);
        assertThat(payload.messageArgs())
                .anyMatch(arg -> arg.text().equals(MessageTextCode.getFilterNameByType(filterType).getResourceName()));
        assertThat(payload.buttons())
                .extracting("code")
                .containsExactly(
                        ButtonTextCode.TEXT_FILTER_DISABLE.name() + ":1:" + filterType.name(),
                        ButtonTextCode.BACK.name() + ":1"
                );
    }

    @Test
    void buildMessagePayloadForUser_whenChatFoundAndFilterDisabled_shouldReturnEnableButton() {
        long chatId = 2L;
        FilterType filterType = FilterType.EMAILS;
        TextFilterSettings filterSettings = new TextFilterSettings();

        GroupChatRecord chatRecord = new GroupChatRecord();
        chatRecord.setId(chatId);
        chatRecord.setName("chatName");

        when(textModerationSettingsService.getFilterSettings(chatId, filterType)).thenReturn(filterSettings);
        when(groupChatService.findById(chatId)).thenReturn(Optional.of(chatRecord));

        AppUserRecord user = new AppUserRecord();

        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{chatId, filterType});

        assertThat(payload.buttons())
                .extracting("code")
                .containsExactly(
                        ButtonTextCode.TEXT_FILTER_ENABLE.name() + ":2:" + filterType.name(),
                        ButtonTextCode.BACK.name() + ":2"
                );
    }

    @Test
    void buildMessagePayloadForUser_whenChatNotFound_shouldReturnChatNotFoundMessage() {
        long chatId = 123L;
        FilterType filterType = FilterType.EMAILS;

        when(groupChatService.findById(chatId)).thenReturn(Optional.empty());
        when(textModerationSettingsService.getFilterSettings(eq(chatId), eq(filterType))).thenReturn(new TextFilterSettings());

        AppUserRecord user = new AppUserRecord();

        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{chatId, filterType});

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
    void processCallbackButtonUpdate_whenBackButtonPressed_shouldReturnTextFiltersState() {
        int messageId = 10;
        long chatId = 99L;

        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(chatId);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.pressedButton()).thenReturn(ButtonTextCode.BACK);

        AppUserRecord user = new AppUserRecord();

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.TEXT_FILTERS);
        assertThat(result.messageId()).isEqualTo(messageId);
        assertThat(result.args()).containsExactly(chatId);
    }

    @Test
    void processCallbackButtonUpdate_whenUnknownButton_shouldReturnStartState() {
        int messageId = 15;
        long chatId = 20L;

        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(chatId);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.pressedButton()).thenReturn(ButtonTextCode.START_LANGUAGE); // не кнопка фильтра

        AppUserRecord user = new AppUserRecord();

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.START);
        assertThat(result.messageId()).isEqualTo(messageId);
    }

    @Test
    void processCallbackButtonUpdate_whenFilterEnableButton_andUserIsAdmin_shouldUpdateFilterAndShowCallback() {
        int messageId = 20;
        long chatId = 33L;
        String callbackId = "cbId";
        String locale = "ru";
        FilterType filterType = FilterType.EMAILS;

        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(chatId);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.callbackId()).thenReturn(callbackId);
        when(callbackData.pressedButton()).thenReturn(ButtonTextCode.TEXT_FILTER_ENABLE);
        when(callbackData.getFilterType()).thenReturn(filterType);
        when(textModerationSettingsService.getFilterSettings(eq(chatId), eq(filterType))).thenReturn(new TextFilterSettings());

        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.ADMIN.name());
        user.setLocale(locale);

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        verify(textModerationSettingsService).updateFilterState(chatId, filterType, true);
        verify(callbackSender).showAnswerCallback(
                argThat(payload -> payload.callbackText() == CallbackTextCode.FILTER_ENABLE),
                eq(locale),
                eq(callbackId)
        );

        assertThat(result.newState()).isEqualTo(UserState.TEXT_FILTER);
        assertThat(result.messageId()).isEqualTo(messageId);
        assertThat(result.args()).containsExactly(chatId, filterType);
    }

    @Test
    void processCallbackButtonUpdate_whenFilterDisableButton_andSettingsNotFound_shouldShowUnavailableCallback() throws Exception {
        int messageId = 21;
        long chatId = 40L;
        String callbackId = "cbId2";
        String locale = "fr";
        FilterType filterType = FilterType.EMAILS;

        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(chatId);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.callbackId()).thenReturn(callbackId);
        when(callbackData.pressedButton()).thenReturn(ButtonTextCode.TEXT_FILTER_DISABLE);
        when(callbackData.getFilterType()).thenReturn(filterType);

        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.ADMIN.name());
        user.setLocale(locale);

        doThrow(new ChatModerationSettingsNotFoundException("")).when(textModerationSettingsService)
                .updateFilterState(chatId, filterType, false);

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        verify(callbackSender).showChatUnavailableCallback(callbackId, locale);

        assertThat(result.newState()).isEqualTo(UserState.TEXT_FILTER);
        assertThat(result.messageId()).isEqualTo(messageId);
        assertThat(result.args()).containsExactly(chatId, filterType);
    }
}