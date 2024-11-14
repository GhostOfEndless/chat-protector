package ru.tbank.processor.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import ru.tbank.common.entity.dto.DeletedTextMessageDTO;
import ru.tbank.processor.generated.tables.DeletedTextMessage;
import ru.tbank.processor.generated.tables.records.DeletedTextMessageRecord;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeletedTextMessageService {

    private final DeletedTextMessage table = DeletedTextMessage.DELETED_TEXT_MESSAGE;
    private final DSLContext dslContext;

    public void save(@NonNull DeletedTextMessageDTO deletedTextMessage) {
        DeletedTextMessageRecord record = dslContext.newRecord(table);

        // setup fields
        record.setChatId(deletedTextMessage.chatId());
        record.setMessageId(deletedTextMessage.messageId());
        record.setMessageText(deletedTextMessage.messageText());
        record.setUserId(deletedTextMessage.userId());
        record.setReason(deletedTextMessage.reason());
        record.store();
    }

    public Optional<DeletedTextMessageRecord> findById(Long id) {
        var fetchedRecord = dslContext.fetchOne(table, table.ID.eq(id));
        return Optional.ofNullable(fetchedRecord);
    }
}
