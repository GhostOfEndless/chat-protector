package ru.tbank.processor.service.personal.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.generated.tables.records.GroupChatRecord;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.CallbackAnswerSender;
import ru.tbank.processor.service.personal.MessageSender;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessageArgument;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatStateHandlerTest {

    private GroupChatService groupChatService;
    private ChatStateHandler handler;

    @BeforeEach
    void setUp() {
        PersonalChatService personalChatService = mock(PersonalChatService.class);
        CallbackAnswerSender callbackSender = mock(CallbackAnswerSender.class);
        MessageSender messageSender = mock(MessageSender.class);
        groupChatService = mock(GroupChatService.class);
        handler = new ChatStateHandler(personalChatService, groupChatService, callbackSender, messageSender);
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnChatPayload_whenChatFound() {
        long chatId = 123L;
        GroupChatRecord chatRecord = new GroupChatRecord();
        chatRecord.setId(chatId);
        chatRecord.setName("Test Chat");

        when(groupChatService.findById(chatId)).thenReturn(Optional.of(chatRecord));

        AppUserRecord user = new AppUserRecord();
        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{chatId});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.CHAT_MESSAGE);
        assertThat(payload.messageArgs())
                .containsExactly(MessageArgument.createTextArgument("Test Chat"));
        assertThat(payload.buttons())
                .containsExactly(
                        CallbackButtonPayload.create(ButtonTextCode.CHAT_FILTERS_SETTINGS, chatId),
                        CallbackButtonPayload.create(ButtonTextCode.CHAT_SPAM_PROTECTION, chatId),
                        CallbackButtonPayload.create(ButtonTextCode.CHAT_EXCLUDE, chatId),
                        CallbackButtonPayload.create(ButtonTextCode.BACK)
                );
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnNotFoundMessage_whenChatNotFound() {
        long chatId = 999L;
        when(groupChatService.findById(chatId)).thenReturn(Optional.empty());

        AppUserRecord user = new AppUserRecord();
        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{chatId});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.CHAT_MESSAGE_NOT_FOUND);
        assertThat(payload.buttons())
                .containsExactly(CallbackButtonPayload.create(ButtonTextCode.BACK));
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnChatsState_whenBackPressed() {
        CallbackData callbackData = new CallbackData(5, "cb", ButtonTextCode.BACK, new String[]{});
        AppUserRecord user = new AppUserRecord();

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.CHATS);
        assertThat(result.messageId()).isEqualTo(5);
    }

    @Test
    void processCallbackButtonUpdate_shouldProcessSpamProtection_whenButtonPressedAndUserIsAdmin() {
        AppUserRecord admin = new AppUserRecord();
        admin.setRole(UserRole.ADMIN.name());

        CallbackData callbackData = new CallbackData(10, "cb", ButtonTextCode.CHAT_SPAM_PROTECTION, new String[]{"42"});

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, admin);

        assertThat(result.newState()).isEqualTo(UserState.SPAM_PROTECTION);
        assertThat(result.messageId()).isEqualTo(10);
        assertThat(result.args()).contains(42L);
    }

    @Test
    void processCallbackButtonUpdate_shouldProcessFiltersSettings_whenButtonPressedAndUserIsAdmin() {
        AppUserRecord admin = new AppUserRecord();
        admin.setRole(UserRole.ADMIN.name());

        CallbackData callbackData = new CallbackData(11, "cb", ButtonTextCode.CHAT_FILTERS_SETTINGS, new String[]{"55"});

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, admin);

        assertThat(result.newState()).isEqualTo(UserState.FILTERS);
        assertThat(result.messageId()).isEqualTo(11);
        assertThat(result.args()).contains(55L);
    }

    @Test
    void processCallbackButtonUpdate_shouldProcessChatExclude_whenButtonPressedAndUserIsAdmin() {
        AppUserRecord admin = new AppUserRecord();
        admin.setRole(UserRole.ADMIN.name());

        CallbackData callbackData = new CallbackData(12, "cb", ButtonTextCode.CHAT_EXCLUDE, new String[]{"77"});

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, admin);

        assertThat(result.newState()).isEqualTo(UserState.CHAT_DELETION);
        assertThat(result.messageId()).isEqualTo(12);
        assertThat(result.args()).contains(77L);
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnStartState_whenUnknownButtonPressed() {
        AppUserRecord user = new AppUserRecord();

        CallbackData callbackData = new CallbackData(13, "cb", ButtonTextCode.START_LANGUAGE, new String[]{});

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.START);
        assertThat(result.messageId()).isEqualTo(13);
    }
}

