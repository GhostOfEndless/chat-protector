package ru.tbank.processor.service.personal.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.generated.tables.records.GroupChatRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.moderation.ChatModerationSettingsService;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.CallbackAnswerSender;
import ru.tbank.processor.service.personal.MessageSender;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatDeletionStateHandlerTest {

    private GroupChatService groupChatService;
    private CallbackAnswerSender callbackSender;
    private ChatModerationSettingsService chatModerationSettingsService;
    private TelegramClientService telegramClientService;

    private ChatDeletionStateHandler handler;

    @BeforeEach
    void setUp() {
        PersonalChatService personalChatService = mock(PersonalChatService.class);
        MessageSender messageSender = mock(MessageSender.class);
        groupChatService = mock(GroupChatService.class);
        callbackSender = mock(CallbackAnswerSender.class);
        chatModerationSettingsService = mock(ChatModerationSettingsService.class);
        telegramClientService = mock(TelegramClientService.class);

        handler = new ChatDeletionStateHandler(
                personalChatService,
                groupChatService,
                callbackSender,
                messageSender,
                chatModerationSettingsService,
                telegramClientService
        );
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnPayloadWithButtons_whenChatExists() {
        long chatId = 123L;
        GroupChatRecord chatRecord = new GroupChatRecord();
        chatRecord.setId(1L);
        chatRecord.setName("Chat One");
        when(groupChatService.findById(chatId)).thenReturn(Optional.of(chatRecord));

        AppUserRecord user = new AppUserRecord();

        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{chatId});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.CHAT_DELETION_MESSAGE);
        assertThat(payload.buttons()).extracting("code")
                .containsExactly(
                        ButtonTextCode.CHAT_DELETION_CONFIRM.name() + ":123",
                        ButtonTextCode.BACK + ":123"
                );
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnChatNotFoundMessage_whenChatNotFound() {
        long chatId = 999L;
        when(groupChatService.findById(chatId)).thenReturn(Optional.empty());

        AppUserRecord user = new AppUserRecord();

        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{chatId});

        assertThat(payload).isEqualTo(handler.chatNotFoundMessage.get());
    }

    @Test
    void processCallbackButtonUpdate_shouldShowUnavailableAndReturnChatsState_whenChatIdZero() {
        int messageId = 10;
        String callbackId = "cb123";
        String userLocale = "en";

        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(0L);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.callbackId()).thenReturn(callbackId);

        AppUserRecord user = new AppUserRecord();
        user.setLocale(userLocale);

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        verify(callbackSender).showChatUnavailableCallback(callbackId, userLocale);
        assertThat(result.newState()).isEqualTo(UserState.CHATS);
        assertThat(result.messageId()).isEqualTo(messageId);
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnChatStateWithChatId_whenBackPressed() {
        long chatId = 42L;
        int messageId = 15;

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
    void processCallbackButtonUpdate_shouldProcessDeletion_whenAdminConfirms() {
        long chatId = 77L;
        int messageId = 21;
        String callbackId = "cb77";
        String userLocale = "ru";

        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(chatId);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.callbackId()).thenReturn(callbackId);
        when(callbackData.pressedButton()).thenReturn(ButtonTextCode.CHAT_DELETION_CONFIRM);

        AppUserRecord adminUser = new AppUserRecord();
        adminUser.setRole(UserRole.ADMIN.name());
        adminUser.setLocale(userLocale);

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, adminUser);

        verify(telegramClientService).leaveFromChat(chatId);
        verify(callbackSender).showAnswerCallback(any(), eq(userLocale), eq(callbackId));
        verify(chatModerationSettingsService).deleteChatConfig(chatId);
        verify(groupChatService).remove(chatId);

        assertThat(result.newState()).isEqualTo(UserState.CHATS);
        assertThat(result.messageId()).isEqualTo(messageId);
        assertThat(result.args()).containsExactly(chatId);
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnStartStateForUnknownButton() {
        int messageId = 33;

        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(5L);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.pressedButton()).thenReturn(ButtonTextCode.START_LANGUAGE);

        AppUserRecord user = new AppUserRecord();

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.START);
        assertThat(result.messageId()).isEqualTo(messageId);
    }
}