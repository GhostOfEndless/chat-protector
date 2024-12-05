package ru.tbank.processor.service.persistence;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import ru.tbank.processor.excpetion.EntityNotFoundException;
import ru.tbank.processor.generated.tables.AppUser;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.personal.enums.UserRole;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUser table = AppUser.APP_USER;
    private final DSLContext dslContext;

    public AppUserRecord save(Long userId, String firstName, String lastName, String username, String role) {
        var storedUser = findById(userId);

        if (storedUser.isPresent()) {
            return storedUser.get();
        }

        var newRecord = dslContext.newRecord(table);

        newRecord.setId(userId);
        newRecord.setFirstName(firstName);
        newRecord.setRole(role);
        newRecord.setLastName(lastName == null? "": lastName);
        newRecord.setUsername(username == null? "": username);
        newRecord.store();

        return findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User with id=%d not found".formatted(userId)));
    }

    public AppUserRecord save(Long userId, String firstName, String lastName, String username) {
        return save(userId, firstName, lastName, username, UserRole.USER.name());
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
