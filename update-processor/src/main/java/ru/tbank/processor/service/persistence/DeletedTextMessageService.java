package ru.tbank.processor.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import ru.tbank.common.telegram.Message;
import ru.tbank.processor.generated.tables.DeletedTextMessage;
import ru.tbank.processor.generated.tables.records.DeletedTextMessageRecord;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeletedTextMessageService {

    private final DeletedTextMessage table = DeletedTextMessage.DELETED_TEXT_MESSAGE;
    private final DSLContext dslContext;

    public void save(@NonNull Message message, String reason) {
        dslContext.insertInto(table)
                .columns(table.CHAT_ID, table.USER_ID, table.MESSAGE_ID, table.MESSAGE_TEXT, table.REASON)
                .values(message.chat().id(), message.user().id(), message.messageId(), message.text(), reason)
                .execute();
    }

    public Optional<DeletedTextMessageRecord> findById(Long id) {
        var fetchedRecord = dslContext.fetchOne(table, table.ID.eq(id));
        return Optional.ofNullable(fetchedRecord);
    }
}
