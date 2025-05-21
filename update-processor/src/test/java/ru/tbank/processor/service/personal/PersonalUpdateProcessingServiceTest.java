package ru.tbank.processor.service.personal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.common.telegram.CallbackEvent;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.TelegramUpdate;
import ru.tbank.common.telegram.User;
import ru.tbank.common.telegram.enums.UpdateType;
import ru.tbank.processor.config.TelegramProperties;
import ru.tbank.processor.excpetion.UserIdParsingException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.handlers.PersonalUpdateHandler;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalUpdateProcessingServiceTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private PersonalChatService personalChatService;

    @Mock
    private TelegramProperties telegramProperties;

    @Mock
    private PersonalUpdateHandler updateHandler;

    @Mock
    private PersonalUpdateHandler startHandler;

    @InjectMocks
    private PersonalUpdateProcessingService service;

    private static final Long USER_ID = 12345L;
    private static final Long OWNER_ID = 54321L;
    private static final String USERNAME = "testUser";
    private static final String NEW_USERNAME = "newTestUser";

    @BeforeEach
    void setUp() {
        lenient().when(updateHandler.getProcessedUserState()).thenReturn(UserState.START);
        lenient().when(startHandler.getProcessedUserState()).thenReturn(UserState.START);
        service = new PersonalUpdateProcessingService(
                appUserService,
                List.of(updateHandler, startHandler),
                personalChatService,
                telegramProperties
        );
        service.init();
    }

    @Test
    void process_personalMessage_newUser_savesUserAndHandlesStartState() {
        TelegramUpdate update = createPersonalMessageUpdate();
        AppUserRecord userRecord = createUserRecord();
        when(personalChatService.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(appUserService.findById(USER_ID)).thenReturn(Optional.empty());
        when(appUserService.save(any(User.class))).thenReturn(userRecord);
        when(startHandler.handle(any(), any())).thenReturn(new ProcessingResult(UserState.START, 0, new Object[]{}));

        service.process(update);

        verify(appUserService).save(any(User.class));
        verify(startHandler).handle(eq(update), eq(userRecord));
    }

    @Test
    void process_userIdZero_throwsUserIdParsingException() {
        TelegramUpdate update = mock(TelegramUpdate.class);
        User user = mock(User.class);
        when(update.updateType()).thenReturn(UpdateType.PERSONAL_MESSAGE);
        when(update.message()).thenReturn(mock(Message.class));
        when(update.message().user()).thenReturn(user);
        when(user.id()).thenReturn(0L);

        assertThrows(UserIdParsingException.class, () -> service.process(update));
    }

    @Test
    void saveNewUser_ownerId_savesAsOwner() {
        User user = new User(OWNER_ID, USERNAME, "First", "Last", null);
        when(telegramProperties.ownerId()).thenReturn(OWNER_ID);
        AppUserRecord userRecord = createUserRecord();
        when(appUserService.save(user, UserRole.OWNER.name())).thenReturn(userRecord);

        AppUserRecord result = service.saveNewUser(user);

        verify(appUserService).save(user, UserRole.OWNER.name());
        assertEquals(userRecord, result);
    }

    @Test
    void saveNewUser_regularUser_savesAsUser() {
        User user = new User(USER_ID, USERNAME, "First", "Last", null);
        when(telegramProperties.ownerId()).thenReturn(OWNER_ID);
        AppUserRecord userRecord = createUserRecord();
        when(appUserService.save(user)).thenReturn(userRecord);

        AppUserRecord result = service.saveNewUser(user);

        verify(appUserService).save(user);
        assertEquals(userRecord, result);
    }

    @Test
    void checkUserName_usernameChanged_updatesUsername() {
        User user = new User(USER_ID, "First", "Last", NEW_USERNAME, null);
        AppUserRecord userRecord = createUserRecord();
        userRecord.setUsername(USERNAME);

        service.checkUserName(user, userRecord);

        verify(appUserService).updateUsername(USER_ID, NEW_USERNAME);
        assertEquals(NEW_USERNAME, userRecord.getUsername());
    }

    @Test
    void checkUserName_sameUsername_noUpdate() {
        User user = new User(USER_ID, "First", "Last", USERNAME, null);
        AppUserRecord userRecord = createUserRecord();

        service.checkUserName(user, userRecord);

        verify(appUserService, never()).updateUsername(anyLong(), anyString());
        assertEquals(USERNAME, userRecord.getUsername());
    }

    @Test
    void parseUserFromUpdate_personalMessage_returnsUser() {
        TelegramUpdate update = createPersonalMessageUpdate();

        User result = service.parseUserFromUpdate(update);

        assertEquals(USER_ID, result.id());
        assertEquals(USERNAME, result.userName());
    }

    @Test
    void parseUserFromUpdate_callbackEvent_returnsUser() {
        TelegramUpdate update = createCallbackEventUpdate();

        User result = service.parseUserFromUpdate(update);

        assertEquals(USER_ID, result.id());
        assertEquals(USERNAME, result.userName());
    }

    private TelegramUpdate createPersonalMessageUpdate() {
        TelegramUpdate update = mock(TelegramUpdate.class);
        Message message = mock(Message.class);
        User user = new User(USER_ID, "First", "Last", USERNAME, null);
        when(update.updateType()).thenReturn(UpdateType.PERSONAL_MESSAGE);
        when(update.message()).thenReturn(message);
        when(message.user()).thenReturn(user);
        return update;
    }

    private TelegramUpdate createCallbackEventUpdate() {
        TelegramUpdate update = mock(TelegramUpdate.class);
        CallbackEvent callbackEvent = mock(CallbackEvent.class);
        User user = new User(USER_ID, "First", "Last", USERNAME,null);
        when(update.updateType()).thenReturn(UpdateType.CALLBACK_EVENT);
        when(update.callbackEvent()).thenReturn(callbackEvent);
        when(callbackEvent.user()).thenReturn(user);
        return update;
    }

    private AppUserRecord createUserRecord() {
        AppUserRecord record = new AppUserRecord();
        record.setId(USER_ID);
        record.setUsername(USERNAME);
        return record;
    }
}
