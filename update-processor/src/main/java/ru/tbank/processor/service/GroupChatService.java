package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import ru.tbank.processor.generated.tables.GroupChat;
import ru.tbank.processor.generated.tables.records.GroupChatRecord;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChat table = GroupChat.GROUP_CHAT;
    private final DSLContext dslContext;

    public void save(Long chatId, String name) {
        var storedUser = findById(chatId);

        if (storedUser.isPresent()) {
            return;
        }

        var newRecord = dslContext.newRecord(table);

        newRecord.setId(chatId);
        newRecord.setName(name);
        newRecord.store();
    }

    public Optional<GroupChatRecord> findById(Long chatId) {
        var fetchedRecord = dslContext.fetchOne(table, table.ID.eq(chatId));

        return Optional.ofNullable(fetchedRecord);
    }
}
