package ru.tbank.processor.service.group;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import ru.tbank.common.entity.dto.DeletedTextMessageDTO;
import ru.tbank.processor.excpetion.EntityNotFoundException;
import ru.tbank.processor.generated.tables.DeletedTextMessage;
import ru.tbank.processor.generated.tables.records.DeletedTextMessageRecord;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeletedTextMessageService {

    private static final DeletedTextMessage table = DeletedTextMessage.DELETED_TEXT_MESSAGE;
    private final DSLContext dslContext;

    public DeletedTextMessageRecord create(@NonNull DeletedTextMessageDTO deletedTextMessage) {
        DeletedTextMessageRecord record = dslContext.newRecord(table);

        // setup fields
        record.setChatId(deletedTextMessage.chatId());
        record.setMessageId(deletedTextMessage.messageId());
        record.setMessageText(deletedTextMessage.messageText());
        record.setUserId(deletedTextMessage.userId());
        record.setUserFullName(deletedTextMessage.userFullName());
        record.setUserUsername(deletedTextMessage.username());
        record.setReason(deletedTextMessage.reason());
        record.store();

        Long id = record.getId();

        return findById(id).orElseThrow(
                () -> new EntityNotFoundException("DeletedTextMessage record with id=%d not found".formatted(id)));
    }

    public Optional<DeletedTextMessageRecord> findById(Long id) {
        var fetchedRecord = dslContext.fetchOne(table, table.ID.eq(id));

        return Optional.ofNullable(fetchedRecord);
    }
}
