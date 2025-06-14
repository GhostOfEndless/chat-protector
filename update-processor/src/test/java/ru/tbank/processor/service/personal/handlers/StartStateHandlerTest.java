package ru.tbank.processor.service.personal.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StartStateHandlerTest {

    private StartStateHandler handler;

    @BeforeEach
    void setUp() {
        PersonalChatService personalChatService = mock(PersonalChatService.class);
        CallbackAnswerSender callbackSender = mock(CallbackAnswerSender.class);
        MessageSender messageSender = mock(MessageSender.class);
        handler = new StartStateHandler(personalChatService, callbackSender, messageSender);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    void buildMessagePayloadForUser_shouldReturnCorrectButtonsAndMessageForEachRole(UserRole role) {
        AppUserRecord user = new AppUserRecord();
        user.setRole(role.name());

        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{});

        switch (role) {
            case USER -> {
                assertThat(payload.messageText()).isEqualTo(MessageTextCode.START_MESSAGE_USER);
                assertThat(payload.buttons()).containsExactly(
                        CallbackButtonPayload.create(ButtonTextCode.START_LANGUAGE)
                );
            }
            case ADMIN -> {
                assertThat(payload.messageText()).isEqualTo(MessageTextCode.START_MESSAGE_ADMIN);
                assertThat(payload.buttons()).containsExactly(
                        CallbackButtonPayload.create(ButtonTextCode.START_CHATS),
                        CallbackButtonPayload.create(ButtonTextCode.START_ACCOUNT),
                        CallbackButtonPayload.create(ButtonTextCode.START_LANGUAGE)
                );
            }
            case OWNER -> {
                assertThat(payload.messageText()).isEqualTo(MessageTextCode.START_MESSAGE_OWNER);
                assertThat(payload.buttons()).containsExactly(
                        CallbackButtonPayload.create(ButtonTextCode.START_CHATS),
                        CallbackButtonPayload.create(ButtonTextCode.START_ADMINS),
                        CallbackButtonPayload.create(ButtonTextCode.START_ACCOUNT),
                        CallbackButtonPayload.create(ButtonTextCode.START_LANGUAGE)
                );
            }
        }
    }

    @Test
    void processCallbackButtonUpdate_shouldGoToLanguageState() {
        CallbackData data = new CallbackData(
                123,
                "cbId",
                ButtonTextCode.START_LANGUAGE,
                new String[]{}
        );
        AppUserRecord user = new AppUserRecord();

        ProcessingResult result = handler.processCallbackButtonUpdate(data, user);

        assertThat(result.newState()).isEqualTo(UserState.LANGUAGE);
        assertThat(result.messageId()).isEqualTo(123);
    }

    @Test
    void processCallbackButtonUpdate_shouldGoToChatsState_whenAdmin() {
        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.ADMIN.name());

        CallbackData data = new CallbackData(
                42,
                "cbId",
                ButtonTextCode.START_CHATS,
                new String[]{}
        );

        ProcessingResult result = handler.processCallbackButtonUpdate(data, user);

        assertThat(result.newState()).isEqualTo(UserState.CHATS);
        assertThat(result.messageId()).isEqualTo(42);
    }

    @Test
    void processCallbackButtonUpdate_shouldGoToAdminsState_whenOwner() {
        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.OWNER.name());

        CallbackData data = new CallbackData(
                55,
                "cbId",
                ButtonTextCode.START_ADMINS,
                new String[]{}
        );

        ProcessingResult result = handler.processCallbackButtonUpdate(data, user);

        assertThat(result.newState()).isEqualTo(UserState.ADMINS);
        assertThat(result.messageId()).isEqualTo(55);
    }

    @Test
    void processCallbackButtonUpdate_shouldGoToAccountState_whenAdmin() {
        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.ADMIN.name());

        CallbackData data = new CallbackData(
                88,
                "cbId",
                ButtonTextCode.START_ACCOUNT,
                new String[]{}
        );

        ProcessingResult result = handler.processCallbackButtonUpdate(data, user);

        assertThat(result.newState()).isEqualTo(UserState.ACCOUNT);
        assertThat(result.messageId()).isEqualTo(88);
    }

    @Test
    void processCallbackButtonUpdate_shouldStayInStartState_whenUnknownButtonPressed() {
        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.USER.name());

        CallbackData data = new CallbackData(
                999,
                "cbId",
                ButtonTextCode.ACCOUNT_CHANGE_PASSWORD,
                new String[]{}
        );

        ProcessingResult result = handler.processCallbackButtonUpdate(data, user);

        assertThat(result.newState()).isEqualTo(UserState.START);
        assertThat(result.messageId()).isEqualTo(999);
    }
}
