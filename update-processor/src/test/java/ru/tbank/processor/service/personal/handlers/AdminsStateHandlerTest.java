package ru.tbank.processor.service.personal.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.persistence.AppUserService;
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

class AdminsStateHandlerTest {

    private AppUserService appUserService;
    private AdminsStateHandler handler;

    @BeforeEach
    void setUp() {
        PersonalChatService personalChatService = mock(PersonalChatService.class);
        CallbackAnswerSender callbackSender = mock(CallbackAnswerSender.class);
        MessageSender messageSender = mock(MessageSender.class);
        appUserService = mock(AppUserService.class);
        handler = new AdminsStateHandler(personalChatService, callbackSender, messageSender, appUserService);
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnAllAdminButtonsPlusAdditionAndBack() {
        var admin1 = new AppUserRecord();
        admin1.setId(1L);
        admin1.setFirstName("Alice");
        admin1.setLastName("Smith");

        var admin2 = new AppUserRecord();
        admin2.setId(2L);
        admin2.setFirstName("Bob");
        admin2.setLastName("Jones");

        when(appUserService.findAllAdmins()).thenReturn(List.of(admin1, admin2));

        var user = new AppUserRecord();
        MessagePayload payload = handler.buildMessagePayloadForUser(user, new Object[]{});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.ADMINS_MESSAGE);
        assertThat(payload.buttons()).containsExactly(
                CallbackButtonPayload.createAdminButton("Alice", "Smith", 1L),
                CallbackButtonPayload.createAdminButton("Bob", "Jones", 2L),
                CallbackButtonPayload.create(ButtonTextCode.ADMINS_ADMIN_ADDITION),
                CallbackButtonPayload.create(ButtonTextCode.BACK)
        );
    }

    @Test
    void processCallbackButtonUpdate_shouldGoToStartState_whenBackPressed() {
        var user = new AppUserRecord();
        CallbackData data = new CallbackData(11, "cbId", ButtonTextCode.BACK, new String[]{});

        ProcessingResult result = handler.processCallbackButtonUpdate(data, user);

        assertThat(result.newState()).isEqualTo(UserState.START);
        assertThat(result.messageId()).isEqualTo(11);
    }

    @Test
    void processCallbackButtonUpdate_shouldGoToAdminAdditionState_whenAdditionPressed_andUserIsOwner() {
        var user = new AppUserRecord();
        user.setRole(UserRole.OWNER.name());

        CallbackData data = new CallbackData(21, "cbId", ButtonTextCode.ADMINS_ADMIN_ADDITION, new String[]{});

        ProcessingResult result = handler.processCallbackButtonUpdate(data, user);

        assertThat(result.newState()).isEqualTo(UserState.ADMIN_ADDITION);
        assertThat(result.messageId()).isEqualTo(21);
    }

    @Test
    void processCallbackButtonUpdate_shouldGoToAdminState_withAdminId_whenAdminButtonPressed_andUserIsOwner() {
        var user = new AppUserRecord();
        user.setRole(UserRole.OWNER.name());

        String adminId = "777";
        CallbackData data = new CallbackData(42, "cbId", ButtonTextCode.ADMINS_ADMIN, new String[]{adminId});

        ProcessingResult result = handler.processCallbackButtonUpdate(data, user);

        assertThat(result.newState()).isEqualTo(UserState.ADMIN);
        assertThat(result.messageId()).isEqualTo(42);
        assertThat(result.args()).containsExactly(777L);
    }

    @Test
    void processCallbackButtonUpdate_shouldStayInSameState_whenUnknownButtonPressed() {
        var user = new AppUserRecord();
        CallbackData data = new CallbackData(99, "cbId", ButtonTextCode.START_LANGUAGE, new String[]{});

        ProcessingResult result = handler.processCallbackButtonUpdate(data, user);

        assertThat(result.newState()).isEqualTo(UserState.ADMINS);
        assertThat(result.messageId()).isEqualTo(99);
    }
}

