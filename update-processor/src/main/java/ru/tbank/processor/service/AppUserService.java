package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import ru.tbank.processor.generated.tables.AppUser;
import ru.tbank.processor.generated.tables.records.AppUserRecord;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUser table = AppUser.APP_USER;
    private final DSLContext dslContext;

    public void saveRegularUser(Long userId, String firstName, String lastName, String username) {
        var storedUser = findById(userId);

        if (storedUser.isPresent()) {
            return;
        }

        var newRecord = dslContext.newRecord(table);

        newRecord.setId(userId);
        newRecord.setFirstName(firstName);
        newRecord.setLastName(lastName);
        newRecord.setUsername(username);
        newRecord.store();
    }

    public Optional<AppUserRecord> findById(Long userId) {
        var fetchedRecord = dslContext.fetchOne(table, table.ID.eq(userId));

        return Optional.ofNullable(fetchedRecord);
    }
}
