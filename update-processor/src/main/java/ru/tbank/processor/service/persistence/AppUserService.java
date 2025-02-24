package ru.tbank.processor.service.persistence;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.common.telegram.User;
import ru.tbank.processor.excpetion.EntityNotFoundException;
import ru.tbank.processor.generated.tables.AppUser;
import ru.tbank.processor.generated.tables.records.AppUserRecord;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final static AppUser TABLE = AppUser.APP_USER;
    private final PasswordEncoder passwordEncoder;
    private final DSLContext dslContext;

    public AppUserRecord save(User user, String role) {
        var storedUser = findById(user.id());
        if (storedUser.isPresent()) {
            return storedUser.get();
        } else {
            String lastName = Optional.ofNullable(user.lastName()).orElse("");
            String username = Optional.ofNullable(user.userName()).orElse("");
            dslContext.insertInto(TABLE)
                    .columns(TABLE.ID, TABLE.FIRST_NAME, TABLE.LAST_NAME, TABLE.USERNAME, TABLE.ROLE)
                    .values(user.id(), user.firstName(), lastName, username, role)
                    .execute();
        }
        return findById(user.id()).orElseThrow(
                () -> new EntityNotFoundException("User with id=%d not found".formatted(user.id())));
    }

    public AppUserRecord save(User user) {
        return save(user, UserRole.USER.name());
    }

    public void updateLocale(Long userId, String locale) {
        dslContext.update(TABLE)
                .set(TABLE.LOCALE, locale)
                .where(TABLE.ID.eq(userId))
                .execute();
    }

    public void updateUserRole(Long userId, String newRole) {
        dslContext.update(TABLE)
                .set(TABLE.ROLE, newRole)
                .where(TABLE.ID.eq(userId))
                .execute();
    }

    public void updatePassword(Long userId, String password) {
        dslContext.update(TABLE)
                .set(TABLE.HASHED_PASSWORD, passwordEncoder.encode(password))
                .where(TABLE.ID.eq(userId))
                .execute();
    }

    public void updateUsername(Long userId, String userName) {
        var newUserName = Optional.ofNullable(userName).orElse("");
        dslContext.update(TABLE)
                .set(TABLE.USERNAME, newUserName)
                .where(TABLE.ID.eq(userId))
                .execute();
    }

    public List<AppUserRecord> findAllAdmins() {
        return dslContext.selectFrom(TABLE)
                .where(TABLE.ROLE.eq(UserRole.ADMIN.name()))
                .fetch();
    }

    public Optional<AppUserRecord> findById(Long userId) {
        return dslContext.fetchOptional(TABLE, TABLE.ID.eq(userId));
    }

    public Optional<AppUserRecord> findByUsername(String username) {
        return dslContext.fetchOptional(TABLE, TABLE.USERNAME.eq(username));
    }
}
