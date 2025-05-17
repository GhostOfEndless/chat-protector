package ru.tbank.processor.service.persistence;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import ru.tbank.processor.generated.tables.PersonalChat;
import ru.tbank.processor.generated.tables.records.PersonalChatRecord;

@Service
@RequiredArgsConstructor
public class PersonalChatService {

    private final PersonalChat table = PersonalChat.PERSONAL_CHAT;
    private final DSLContext dslContext;

    public void save(Long userId, String state, Integer lastMessageId) {
        var storedUser = findByUserId(userId);

        if (storedUser.isPresent()) {
            update(userId, state, lastMessageId);
        } else {
            dslContext.insertInto(table)
                    .columns(table.USER_ID, table.LAST_MESSAGE_ID, table.STATE)
                    .values(userId, lastMessageId, state)
                    .execute();
        }
    }

    public void update(Long userId, String state, Integer lastMessageId) {
        dslContext.update(table)
                .set(table.STATE, state)
                .set(table.LAST_MESSAGE_ID, lastMessageId)
                .where(table.USER_ID.eq(userId))
                .execute();
    }

    public Optional<PersonalChatRecord> findByUserId(Long userId) {
        return dslContext.fetchOptional(table, table.USER_ID.eq(userId));
    }
}
