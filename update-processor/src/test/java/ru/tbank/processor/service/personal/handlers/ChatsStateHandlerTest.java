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
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatsStateHandlerTest {

    private GroupChatService groupChatService;
    private ChatsStateHandler handler;

    @BeforeEach
    void setUp() {
        PersonalChatService personalChatService = mock(PersonalChatService.class);
        groupChatService = mock(GroupChatService.class);
        CallbackAnswerSender callbackSender = mock(CallbackAnswerSender.class);
        MessageSender messageSender = mock(MessageSender.class);
        handler = new ChatsStateHandler(personalChatService, groupChatService, callbackSender, messageSender);
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnChatsMessageOwner_withChatAdditionButtonForOwner() {
        GroupChatRecord chat1 = new GroupChatRecord();
        GroupChatRecord chat2 = new GroupChatRecord();
        chat1.setId(1L);
        chat1.setName("Chat One");
        chat2.setId(2L);
        chat2.setName("Chat Two");
        when(groupChatService.findAll()).thenReturn(List.of(chat1, chat2));

        AppUserRecord owner = new AppUserRecord();
        owner.setRole(UserRole.OWNER.name());

        MessagePayload payload = handler.buildMessagePayloadForUser(owner, new Object[]{});

        var buttons = payload.buttons();
        assertThat(buttons).hasSize(4);
        assertThat(buttons).extracting("code").containsExactly(
                ButtonTextCode.CHATS_CHAT + ":1",
                ButtonTextCode.CHATS_CHAT + ":2",
                ButtonTextCode.CHATS_CHAT_ADDITION.name(),
                ButtonTextCode.BACK.name()
        );
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnChatsMessageAdmin_withoutChatAdditionButtonForNonOwner() {
        GroupChatRecord chat1 = new GroupChatRecord();
        chat1.setId(1L);
        chat1.setName("Chat One");
        when(groupChatService.findAll()).thenReturn(List.of(chat1));

        AppUserRecord admin = new AppUserRecord();
        admin.setRole(UserRole.ADMIN.name());

        MessagePayload payload = handler.buildMessagePayloadForUser(admin, new Object[]{});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.CHATS_MESSAGE_ADMIN);
        var buttons = payload.buttons();
        assertThat(buttons).hasSize(2);
        assertThat(buttons).extracting("code").containsExactly(
                ButtonTextCode.CHATS_CHAT.name() + ":1",
                ButtonTextCode.BACK.name()
        );
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnStartState_whenBackButtonPressed() {
        CallbackData callbackData = new CallbackData(5, "cb", ButtonTextCode.BACK, new String[]{});
        AppUserRecord user = new AppUserRecord();

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.START);
        assertThat(result.messageId()).isEqualTo(5);
    }

    @Test
    void processCallbackButtonUpdate_shouldProcessChatAddition_whenOwnerPressed() {
        AppUserRecord owner = new AppUserRecord();
        owner.setRole(UserRole.OWNER.name());

        CallbackData callbackData = new CallbackData(10, "cb", ButtonTextCode.CHATS_CHAT_ADDITION, new String[]{});

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, owner);

        assertThat(result.newState()).isEqualTo(UserState.CHAT_ADDITION);
        assertThat(result.messageId()).isEqualTo(10);
    }

    @Test
    void processCallbackButtonUpdate_shouldNotProcessChatAddition_whenNonOwnerPressed() {
        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.ADMIN.name());

        CallbackData callbackData = new CallbackData(10, "cb", ButtonTextCode.CHATS_CHAT_ADDITION, new String[]{});

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.START);
        assertThat(result.messageId()).isEqualTo(10);
    }

    @Test
    void processCallbackButtonUpdate_shouldProcessChatButton_whenAdminPressed() {
        AppUserRecord admin = new AppUserRecord();
        admin.setRole(UserRole.ADMIN.name());

        CallbackData callbackData = new CallbackData(11, "cb", ButtonTextCode.CHATS_CHAT, new String[]{"42"});

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, admin);

        assertThat(result.newState()).isEqualTo(UserState.CHAT);
        assertThat(result.messageId()).isEqualTo(11);
        assertThat(result.args()).contains(42L);
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnProcessedUserState_whenUnknownButton() {
        AppUserRecord user = new AppUserRecord();

        CallbackData callbackData = new CallbackData(12, "cb", ButtonTextCode.START_LANGUAGE, new String[]{});

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.CHATS);
        assertThat(result.messageId()).isEqualTo(12);
    }

    @Test
    void buildChatButtons_shouldCreateButtonsForAllChats() {
        GroupChatRecord chat1 = new GroupChatRecord();
        GroupChatRecord chat2 = new GroupChatRecord();
        chat1.setId(1L);
        chat1.setName("Chat One");
        chat2.setId(2L);
        chat2.setName("Chat Two");

        List<CallbackButtonPayload> buttons = handler.buildChatButtons(List.of(chat1, chat2));

        assertThat(buttons).hasSize(2);
        assertThat(buttons.get(0).code()).isEqualTo("CHATS_CHAT:1");
        assertThat(buttons.get(1).code()).isEqualTo("CHATS_CHAT:2");
    }
}
