package ru.tbank.processor.service.persistence;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.UpdatableRecordImpl;
import org.springframework.stereotype.Service;
import ru.tbank.processor.excpetion.EntityNotFoundException;
import ru.tbank.processor.generated.tables.GroupChat;
import ru.tbank.processor.generated.tables.records.GroupChatRecord;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChat table = GroupChat.GROUP_CHAT;
    private final DSLContext dslContext;

    public GroupChatRecord save(Long chatId, String name) {
        var storedUser = findById(chatId);

        if (storedUser.isPresent()) {
            return storedUser.get();
        }

        var newRecord = dslContext.newRecord(table);

        newRecord.setId(chatId);
        newRecord.setName(name);
        newRecord.store();

        return findById(chatId).orElseThrow(
                () -> new EntityNotFoundException("Chat with id=%d not found".formatted(chatId)));
    }

    public List<GroupChatRecord> findAll() {
        return dslContext.selectFrom(table).fetch();
    }

    public Optional<GroupChatRecord> findById(Long chatId) {
        return dslContext.fetchOptional(table, table.ID.eq(chatId));
    }

    public void remove(Long chatId) {
        dslContext.fetchOptional(table, table.ID.eq(chatId))
                .ifPresent(UpdatableRecordImpl::delete);
    }
}
