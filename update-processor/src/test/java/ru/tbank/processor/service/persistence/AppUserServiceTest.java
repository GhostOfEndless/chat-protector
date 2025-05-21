package ru.tbank.processor.service.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.jooq.DSLContext;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.common.telegram.User;
import ru.tbank.processor.generated.tables.AppUser;
import ru.tbank.processor.generated.tables.records.AppUserRecord;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DSLContext dslContext;

    @InjectMocks
    private AppUserService appUserService;

    private static final Long USER_ID = 12345L;
    private static final String USERNAME = "testUser";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String LOCALE = "en";
    private static final String PASSWORD = "password123";
    private static final String HASHED_PASSWORD = "hashedPassword123";

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
    }

    @Test
    void save_existingUser_returnsExistingRecord() {
        User user = new User(USER_ID, USERNAME, FIRST_NAME, LAST_NAME, null);
        AppUserRecord record = createAppUserRecord();
        when(dslContext.fetchOptional(AppUser.APP_USER, AppUser.APP_USER.ID.eq(USER_ID)))
                .thenReturn(Optional.of(record));

        AppUserRecord result = appUserService.save(user, UserRole.USER.name());

        assertEquals(record, result);
        verify(dslContext).fetchOptional(AppUser.APP_USER, AppUser.APP_USER.ID.eq(USER_ID));
        verify(dslContext, never()).insertInto(any());
    }

    @Test
    void findById_userExists_returnsRecord() {
        AppUserRecord record = createAppUserRecord();
        when(dslContext.fetchOptional(AppUser.APP_USER, AppUser.APP_USER.ID.eq(USER_ID)))
                .thenReturn(Optional.of(record));

        Optional<AppUserRecord> result = appUserService.findById(USER_ID);

        assertTrue(result.isPresent());
        assertEquals(record, result.get());
        verify(dslContext).fetchOptional(AppUser.APP_USER, AppUser.APP_USER.ID.eq(USER_ID));
    }

    @Test
    void findById_userNotFound_returnsEmpty() {
        when(dslContext.fetchOptional(AppUser.APP_USER, AppUser.APP_USER.ID.eq(USER_ID)))
                .thenReturn(Optional.empty());

        Optional<AppUserRecord> result = appUserService.findById(USER_ID);

        assertTrue(result.isEmpty());
        verify(dslContext).fetchOptional(AppUser.APP_USER, AppUser.APP_USER.ID.eq(USER_ID));
    }

    @Test
    void findByUsername_userExists_returnsRecord() {
        AppUserRecord record = createAppUserRecord();
        when(dslContext.fetchOptional(AppUser.APP_USER, AppUser.APP_USER.USERNAME.eq(USERNAME)))
                .thenReturn(Optional.of(record));

        Optional<AppUserRecord> result = appUserService.findByUsername(USERNAME);

        assertTrue(result.isPresent());
        assertEquals(record, result.get());
        verify(dslContext).fetchOptional(AppUser.APP_USER, AppUser.APP_USER.USERNAME.eq(USERNAME));
    }

    @Test
    void findByUsername_userNotFound_returnsEmpty() {
        when(dslContext.fetchOptional(AppUser.APP_USER, AppUser.APP_USER.USERNAME.eq(USERNAME)))
                .thenReturn(Optional.empty());

        Optional<AppUserRecord> result = appUserService.findByUsername(USERNAME);

        assertTrue(result.isEmpty());
        verify(dslContext).fetchOptional(AppUser.APP_USER, AppUser.APP_USER.USERNAME.eq(USERNAME));
    }

    private AppUserRecord createAppUserRecord() {
        AppUserRecord record = new AppUserRecord();
        record.setId(USER_ID);
        record.setUsername(USERNAME);
        record.setFirstName(FIRST_NAME);
        record.setLastName(LAST_NAME);
        record.setRole(UserRole.USER.name());
        return record;
    }
}
