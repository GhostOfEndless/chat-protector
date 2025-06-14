package ru.tbank.processor.service.personal.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.verify;

class AccountStateHandlerTest {

    private PersonalChatService personalChatService;
    private CallbackAnswerSender callbackSender;
    private MessageSender messageSender;
    private AccountStateHandler handler;

    @BeforeEach
    void setUp() {
        personalChatService = mock(PersonalChatService.class);
        callbackSender = mock(CallbackAnswerSender.class);
        messageSender = mock(MessageSender.class);
        handler = new AccountStateHandler(personalChatService, callbackSender, messageSender);
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnUsernameErrorPayload_whenUsernameIsBlank() {
        AppUserRecord userRecord = new AppUserRecord();
        userRecord.setUsername("   ");
        userRecord.setHashedPassword("hashed");

        MessagePayload payload = handler.buildMessagePayloadForUser(userRecord, new Object[]{});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.ACCOUNT_MESSAGE_USERNAME_ERROR);
        assertThat(payload.buttons())
                .extracting(CallbackButtonPayload::code)
                .containsExactly(ButtonTextCode.BACK.name());
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnPasswordNotSetPayload_whenPasswordIsNull() {
        AppUserRecord userRecord = new AppUserRecord();
        userRecord.setUsername("test_user");
        userRecord.setHashedPassword(null);

        MessagePayload payload = handler.buildMessagePayloadForUser(userRecord, new Object[]{});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.ACCOUNT_MESSAGE_PASSWORD_NOT_SET);
        assertThat(payload.messageArgs()).hasSize(1);
        assertThat(payload.messageArgs().getFirst().text()).isEqualTo("test_user");
        assertThat(payload.buttons())
                .extracting(CallbackButtonPayload::code)
                .containsExactly(ButtonTextCode.ACCOUNT_CHANGE_PASSWORD.name(), ButtonTextCode.BACK.name());
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnDefaultPayload_whenUsernameAndPasswordAreSet() {
        AppUserRecord userRecord = new AppUserRecord();
        userRecord.setUsername("valid_user");
        userRecord.setHashedPassword("hashed");

        MessagePayload payload = handler.buildMessagePayloadForUser(userRecord, new Object[]{});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.ACCOUNT_MESSAGE);
        assertThat(payload.messageArgs()).hasSize(1);
        assertThat(payload.messageArgs().getFirst().text()).isEqualTo("valid_user");
        assertThat(payload.buttons())
                .extracting(CallbackButtonPayload::code)
                .containsExactly(ButtonTextCode.ACCOUNT_CHANGE_PASSWORD.name(), ButtonTextCode.BACK.name());
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnStartState_whenPressedBack() {
        CallbackData data = new CallbackData(123, "callback-id", ButtonTextCode.BACK, new String[]{});
        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.USER.name());

        ProcessingResult result = handler.processCallbackButtonUpdate(data, user);

        assertThat(result.newState()).isEqualTo(UserState.START);
        assertThat(result.messageId()).isEqualTo(123);
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnChangePassword_whenUserIsAdmin() {
        CallbackData data = new CallbackData(456, "callback-id", ButtonTextCode.ACCOUNT_CHANGE_PASSWORD, new String[]{});
        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.ADMIN.name());

        ProcessingResult result = handler.processCallbackButtonUpdate(data, user);

        assertThat(result.newState()).isEqualTo(UserState.CHANGE_PASSWORD);
        assertThat(result.messageId()).isEqualTo(456);
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnStartAndCallPermissionDenied_whenUserIsNotAdmin() {
        CallbackData data = new CallbackData(789, "cb-id", ButtonTextCode.ACCOUNT_CHANGE_PASSWORD, new String[]{});
        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.USER.name());
        user.setLocale("ru");

        ProcessingResult result = handler.processCallbackButtonUpdate(data, user);

        assertThat(result.newState()).isEqualTo(UserState.START);
        assertThat(result.messageId()).isEqualTo(789);

        verify(callbackSender).showPermissionDeniedCallback("ru", "cb-id");
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnSameState_whenUnknownButton() {
        CallbackData data = new CallbackData(321, "cb-id", ButtonTextCode.ADMIN_REMOVE, new String[]{});
        AppUserRecord user = new AppUserRecord();
        user.setRole(UserRole.OWNER.name());

        ProcessingResult result = handler.processCallbackButtonUpdate(data, user);

        assertThat(result.newState()).isEqualTo(UserState.ACCOUNT);
        assertThat(result.messageId()).isEqualTo(321);
    }
}
