package ru.tbank.processor.service.persistence;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.tbank.common.telegram.User;
import ru.tbank.processor.excpetion.EntityNotFoundException;
import ru.tbank.processor.generated.tables.AppUser;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.common.entity.enums.UserRole;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final static AppUser table = AppUser.APP_USER;
    private final PasswordEncoder passwordEncoder;
    private final DSLContext dslContext;

    public AppUserRecord save(User user, String role) {
        var storedUser = findById(user.id());
        if (storedUser.isPresent()) {
            return storedUser.get();
        } else {
            String lastName = Optional.ofNullable(user.lastName()).orElse("");
            String username = Optional.ofNullable(user.userName()).orElse("");
            dslContext.insertInto(table)
                    .columns(table.ID, table.FIRST_NAME, table.LAST_NAME, table.USERNAME, table.ROLE)
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
        dslContext.update(table)
                .set(table.LOCALE, locale)
                .where(table.ID.eq(userId))
                .execute();
    }

    public void updateUserRole(Long userId, String newRole) {
        dslContext.update(table)
                .set(table.ROLE, newRole)
                .where(table.ID.eq(userId))
                .execute();
    }

    public void updatePassword(Long userId, String password) {
        dslContext.update(table)
                .set(table.HASHED_PASSWORD, passwordEncoder.encode(password))
                .where(table.ID.eq(userId))
                .execute();
    }

    public void updateUsername(Long userId, String newUserName) {
        newUserName = Optional.ofNullable(newUserName).orElse("");
        dslContext.update(table)
                .set(table.USERNAME, newUserName)
                .where(table.ID.eq(userId))
                .execute();
    }

    public List<AppUserRecord> findAllAdmins() {
        return dslContext.selectFrom(table)
                .where(table.ROLE.eq(UserRole.ADMIN.name()))
                .fetch();
    }

    public Optional<AppUserRecord> findById(Long userId) {
        return dslContext.fetchOptional(table, table.ID.eq(userId));
    }

    public Optional<AppUserRecord> findByUsername(String username) {
        return dslContext.fetchOptional(table, table.USERNAME.eq(username));
    }
}
