package ru.tbank.processor.service.personal.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tbank.common.entity.enums.FilterType;
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
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TextFiltersStateHandlerTest {

    private GroupChatService groupChatService;
    private CallbackAnswerSender callbackSender;
    private TextFiltersStateHandler handler;

    @BeforeEach
    void setUp() {
        groupChatService = mock(GroupChatService.class);
        PersonalChatService personalChatService = mock(PersonalChatService.class);
        callbackSender = mock(CallbackAnswerSender.class);
        MessageSender messageSender = mock(MessageSender.class);

        handler = new TextFiltersStateHandler(
                personalChatService,
                groupChatService,
                callbackSender,
                messageSender
        );
    }

    @Test
    void buildMessagePayloadForUser_whenChatExists_shouldReturnPayloadWithAllButtons() {
        long chatId = 123L;
        AppUserRecord user = new AppUserRecord();
        GroupChatRecord chatRecord = new GroupChatRecord();
        chatRecord.setId(chatId);
        chatRecord.setName("TestChat");

        when(groupChatService.findById(chatId)).thenReturn(Optional.of(chatRecord));

        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{chatId});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.TEXT_FILTERS_MESSAGE);
        assertThat(payload.buttons()).hasSize(8);
        assertThat(payload.buttons()).extracting("code")
                .containsExactly(
                        ButtonTextCode.TEXT_FILTERS_TAGS.name() + ":123",
                        ButtonTextCode.TEXT_FILTERS_EMAILS.name() + ":123",
                        ButtonTextCode.TEXT_FILTERS_LINKS.name() + ":123",
                        ButtonTextCode.TEXT_FILTERS_MENTIONS.name() + ":123",
                        ButtonTextCode.TEXT_FILTERS_BOT_COMMANDS.name() + ":123",
                        ButtonTextCode.TEXT_FILTERS_CUSTOM_EMOJIS.name() + ":123",
                        ButtonTextCode.TEXT_FILTERS_PHONE_NUMBERS.name() + ":123",
                        ButtonTextCode.BACK.name() + ":123"
                );
    }

    @Test
    void buildMessagePayloadForUser_whenChatNotFound_shouldReturnChatNotFoundPayload() {
        long chatId = 999L;
        AppUserRecord user = new AppUserRecord();
        when(groupChatService.findById(chatId)).thenReturn(Optional.empty());

        MessagePayload result = handler.buildMessagePayloadForUser(user, new Object[]{chatId});

        assertThat(result).isEqualTo(handler.chatNotFoundMessage.get());
    }

    @Test
    void processCallbackButtonUpdate_whenChatIdIsZero_shouldShowUnavailableAndReturnChatsState() {
        CallbackData callbackData = mock(CallbackData.class);
        AppUserRecord user = new AppUserRecord();
        user.setLocale("en");

        when(callbackData.getChatId()).thenReturn(0L);
        when(callbackData.messageId()).thenReturn(10);
        when(callbackData.callbackId()).thenReturn("cb-id");

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        verify(callbackSender).showChatUnavailableCallback("cb-id", "en");
        assertThat(result.newState()).isEqualTo(UserState.CHATS);
        assertThat(result.messageId()).isEqualTo(10);
    }

    @Test
    void processCallbackButtonUpdate_whenBackButtonPressed_shouldReturnFiltersState() {
        long chatId = 42L;
        int messageId = 77;
        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(chatId);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.pressedButton()).thenReturn(ButtonTextCode.BACK);

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, new AppUserRecord());

        assertThat(result.newState()).isEqualTo(UserState.FILTERS);
        assertThat(result.args()).containsExactly(chatId);
    }

    @Test
    void processCallbackButtonUpdate_whenInvalidButton_shouldReturnStartState() {
        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(1L);
        when(callbackData.messageId()).thenReturn(15);
        when(callbackData.pressedButton()).thenReturn(ButtonTextCode.START_LANGUAGE); // Некорректная кнопка

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, new AppUserRecord());

        assertThat(result.newState()).isEqualTo(UserState.START);
        assertThat(result.messageId()).isEqualTo(15);
    }

    @Test
    void processCallbackButtonUpdate_whenFilterButtonAndUserIsAdmin_shouldReturnTextFilterState() {
        long chatId = 101L;
        int messageId = 88;
        String callbackId = "cb123";
        FilterType expectedFilter = FilterType.TAGS;

        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.ADMIN.name());

        CallbackData callbackData = mock(CallbackData.class);
        when(callbackData.getChatId()).thenReturn(chatId);
        when(callbackData.messageId()).thenReturn(messageId);
        when(callbackData.callbackId()).thenReturn(callbackId);
        when(callbackData.pressedButton()).thenReturn(ButtonTextCode.TEXT_FILTERS_TAGS);

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.TEXT_FILTER);
        assertThat(result.messageId()).isEqualTo(messageId);
        assertThat(result.args()).containsExactly(chatId, expectedFilter);
    }
}
