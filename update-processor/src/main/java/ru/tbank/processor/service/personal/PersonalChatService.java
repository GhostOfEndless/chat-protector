package ru.tbank.processor.service.personal;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import ru.tbank.processor.generated.tables.PersonalChat;
import ru.tbank.processor.generated.tables.records.PersonalChatRecord;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonalChatService {

    private final PersonalChat table = PersonalChat.PERSONAL_CHAT;
    private final DSLContext dslContext;

    public Optional<PersonalChatRecord> findByUserId(Long userId) {
        var fetchedRecord = dslContext.fetchOne(table, table.USER_ID.eq(userId));

        return Optional.ofNullable(fetchedRecord);
    }
}
