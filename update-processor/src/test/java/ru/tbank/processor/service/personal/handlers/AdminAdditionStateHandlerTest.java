package ru.tbank.processor.service.personal.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.MessageEntity;
import ru.tbank.common.telegram.enums.MessageEntityType;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.CallbackAnswerSender;
import ru.tbank.processor.service.personal.MessageSender;
import ru.tbank.processor.service.personal.enums.AdminAdditionResult;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAdditionStateHandlerTest {

    @Mock
    private PersonalChatService personalChatService;

    @Mock
    private CallbackAnswerSender callbackSender;

    @Mock
    private MessageSender messageSender;

    @Mock
    private AppUserService appUserService;

    @InjectMocks
    private AdminAdditionStateHandler handler;

    private final AppUserRecord adminUser = new AppUserRecord();

    @BeforeEach
    void setUp() {
        adminUser.setId(1L);
        adminUser.setRole(UserRole.ADMIN.name());
        adminUser.setLocale("en");
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnSuccessPayload() {
        MessagePayload payload = handler.buildMessagePayloadForUser(adminUser, new Object[]{AdminAdditionResult.SUCCESS});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.ADMIN_ADDITION_MESSAGE_SUCCESS);
        assertThat(payload.buttons()).hasSize(1);
        assertThat(payload.buttons().getFirst().code()).isEqualTo(ButtonTextCode.BACK.name());
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnUserNotFoundPayload() {
        MessagePayload payload = handler.buildMessagePayloadForUser(adminUser, new Object[]{AdminAdditionResult.USER_NOT_FOUND});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.ADMIN_ADDITION_MESSAGE_USER_NOT_FOUND);
        assertThat(payload.buttons()).hasSize(1);
        assertThat(payload.buttons().getFirst().code()).isEqualTo(ButtonTextCode.BACK.name());
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnUserIsAdminPayload() {
        MessagePayload payload = handler.buildMessagePayloadForUser(adminUser, new Object[]{AdminAdditionResult.USER_IS_ADMIN});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.ADMIN_ADDITION_MESSAGE_USER_IS_ADMIN);
    }

    @Test
    void buildMessagePayloadForUser_shouldReturnDefaultPayload_whenNoArgs() {
        MessagePayload payload = handler.buildMessagePayloadForUser(adminUser, new Object[]{});

        assertThat(payload.messageText()).isEqualTo(MessageTextCode.ADMIN_ADDITION_MESSAGE);
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnAdminsState_whenBackButtonPressed() {
        CallbackData data = new CallbackData(123, "cb-id", ButtonTextCode.BACK, new String[]{});

        ProcessingResult result = handler.processCallbackButtonUpdate(data, adminUser);

        assertThat(result.newState()).isEqualTo(UserState.ADMINS);
        assertThat(result.messageId()).isEqualTo(123);
    }

    @Test
    void processCallbackButtonUpdate_shouldReturnSameState_whenUnknownButton() {
        CallbackData data = new CallbackData(123, "cb-id", ButtonTextCode.ACCOUNT_CHANGE_PASSWORD, new String[]{});

        ProcessingResult result = handler.processCallbackButtonUpdate(data, adminUser);

        assertThat(result.newState()).isEqualTo(UserState.ADMIN_ADDITION);
        assertThat(result.messageId()).isEqualTo(123);
    }

    @Test
    void processTextMessageUpdate_shouldAddAdminAndReturnProcessedState_whenUserFoundAndNotAdmin() {
        Message message = mock(Message.class);
        MessageEntity entity = new MessageEntity(MessageEntityType.MENTION, null, "@newuser");

        when(message.hasEntities()).thenReturn(true);
        when(message.entities()).thenReturn(List.of(entity));
        when(appUserService.findByUsername("newuser")).thenReturn(Optional.of(createUser("USER", 5L)));

        ProcessingResult result = handler.processTextMessageUpdate(message, adminUser);

        assertThat(result.newState()).isEqualTo(UserState.ADMIN_ADDITION);
        verify(appUserService).updateUserRole(5L, UserRole.ADMIN.name());
        verify(messageSender).updateUserMessage(any(), any(), any(), any());
    }

    @Test
    void processTextMessageUpdate_shouldReturnUserIsAdmin_whenUserAlreadyAdmin() {
        Message message = mock(Message.class);
        MessageEntity entity = new MessageEntity(MessageEntityType.MENTION, null, "@existing");

        when(message.hasEntities()).thenReturn(true);
        when(message.entities()).thenReturn(List.of(entity));
        when(appUserService.findByUsername("existing")).thenReturn(Optional.of(createUser("ADMIN", 10L)));

        ProcessingResult result = handler.processTextMessageUpdate(message, adminUser);

        assertThat(result.newState()).isEqualTo(UserState.ADMIN_ADDITION);
        verify(appUserService, never()).updateUserRole(any(), any());
        verify(messageSender).updateUserMessage(any(), any(), any(), any());
    }

    @Test
    void processTextMessageUpdate_shouldReturnUserNotFound_whenUserIsMissing() {
        Message message = mock(Message.class);
        MessageEntity entity = new MessageEntity(MessageEntityType.MENTION, null, "@missing");

        when(message.hasEntities()).thenReturn(true);
        when(message.entities()).thenReturn(List.of(entity));
        when(appUserService.findByUsername("missing")).thenReturn(Optional.empty());

        ProcessingResult result = handler.processTextMessageUpdate(message, adminUser);

        assertThat(result.newState()).isEqualTo(UserState.ADMIN_ADDITION);
        verify(appUserService, never()).updateUserRole(any(), any());
        verify(messageSender).updateUserMessage(any(), any(), any(), any());
    }

    @Test
    void processTextMessageUpdate_shouldFallbackToSuper_whenNoMentions() {
        Message message = mock(Message.class);

        when(message.hasEntities()).thenReturn(false);
        when(message.hasText()).thenReturn(true);
        when(message.text()).thenReturn("Just text");

        ProcessingResult result = handler.processTextMessageUpdate(message, adminUser);

        assertThat(result.newState()).isEqualTo(UserState.ADMIN_ADDITION);
    }

    private AppUserRecord createUser(String role, Long id) {
        AppUserRecord user = new AppUserRecord();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
