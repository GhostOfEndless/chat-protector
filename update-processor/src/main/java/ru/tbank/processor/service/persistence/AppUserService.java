package ru.tbank.processor.service.persistence;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import ru.tbank.processor.excpetion.EntityNotFoundException;
import ru.tbank.processor.generated.tables.AppUser;
import ru.tbank.processor.generated.tables.records.AppUserRecord;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUser table = AppUser.APP_USER;
    private final DSLContext dslContext;

    public AppUserRecord saveRegularUser(Long userId, String firstName, String lastName, String username) {
        var storedUser = findById(userId);

        if (storedUser.isPresent()) {
            return storedUser.get();
        }

        var newRecord = dslContext.newRecord(table);

        newRecord.setId(userId);
        newRecord.setFirstName(firstName);
        newRecord.setLastName(lastName);
        newRecord.setUsername(username);
        newRecord.store();

        return findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User with id=%d not found".formatted(userId)));
    }

    public Optional<AppUserRecord> findById(Long userId) {
        var fetchedRecord = dslContext.fetchOne(table, table.ID.eq(userId));

        return Optional.ofNullable(fetchedRecord);
    }
}
