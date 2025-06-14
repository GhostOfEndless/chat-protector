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
import ru.tbank.processor.service.personal.payload.MessageArgument;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminStateHandlerTest {

    private AppUserService appUserService;
    private AdminStateHandler handler;

    @BeforeEach
    void setUp() {
        PersonalChatService personalChatService = mock(PersonalChatService.class);
        CallbackAnswerSender callbackSender = mock(CallbackAnswerSender.class);
        MessageSender messageSender = mock(MessageSender.class);
        appUserService = mock(AppUserService.class);
        handler = new AdminStateHandler(personalChatService, callbackSender, messageSender, appUserService);
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnAdminPayload_whenAdminFound() {
        AppUserRecord admin = new AppUserRecord();
        admin.setId(5L);
        admin.setFirstName("John");
        admin.setLastName("Doe");

        when(appUserService.findById(5L)).thenReturn(Optional.of(admin));

        AppUserRecord requester = new AppUserRecord();
        MessagePayload payload = handler.buildMessagePayloadForUser(requester, new Object[]{5L});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.ADMIN_MESSAGE);
        assertThat(payload.messageArgs())
                .containsExactly(MessageArgument.createTextArgument("John Doe"));
        assertThat(payload.buttons())
                .containsExactly(
                        CallbackButtonPayload.create(ButtonTextCode.ADMIN_REMOVE, 5L),
                        CallbackButtonPayload.create(ButtonTextCode.BACK)
                );
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnNotFoundPayload_whenAdminNotFound() {
        when(appUserService.findById(999L)).thenReturn(Optional.empty());

        AppUserRecord requester = new AppUserRecord();
        MessagePayload payload = handler.buildMessagePayloadForUser(requester, new Object[]{999L});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.ADMIN_MESSAGE_NOT_FOUND);
        assertThat(payload.buttons()).containsExactly(CallbackButtonPayload.create(ButtonTextCode.BACK));
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnToAdminsState_whenBackPressed() {
        AppUserRecord user = new AppUserRecord();
        CallbackData callbackData = new CallbackData(1, "cbId", ButtonTextCode.BACK, new String[]{});

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.ADMINS);
        assertThat(result.messageId()).isEqualTo(1);
    }

    @Test
    void processCallbackButtonUpdate_shouldRemoveAdminAndReturnToAdminsState_whenAdminRemovePressed() {
        AppUserRecord owner = new AppUserRecord();
        owner.setRole(UserRole.OWNER.name());

        CallbackData callbackData = new CallbackData(10, "cbId", ButtonTextCode.ADMIN_REMOVE, new String[]{"42"});

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, owner);

        verify(appUserService).updateUserRole(42L, UserRole.USER.name());
        assertThat(result.newState()).isEqualTo(UserState.ADMINS);
        assertThat(result.messageId()).isEqualTo(10);
    }

    @Test
    void processCallbackButtonUpdate_shouldStayInAdminState_whenUnknownButtonPressed() {
        AppUserRecord user = new AppUserRecord();
        CallbackData callbackData = new CallbackData(5, "cbId", ButtonTextCode.START_LANGUAGE, new String[]{});

        ProcessingResult result = handler.processCallbackButtonUpdate(callbackData, user);

        assertThat(result.newState()).isEqualTo(UserState.ADMIN);
        assertThat(result.messageId()).isEqualTo(5);
    }
}