package ru.tbank.admin.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.tbank.admin.entity.DeletedMessage;
import ru.tbank.admin.generated.tables.DeletedTextMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeletedMessageService {

    private final static DeletedTextMessage table = DeletedTextMessage.DELETED_TEXT_MESSAGE;
    private final DSLContext dslContext;

    public Page<DeletedMessage> getDeletedMessages(Long chatId, @NonNull Pageable pageable) {
        var records = dslContext.selectFrom(table)
                .where(table.CHAT_ID.eq(chatId))
                .orderBy(table.DELETION_TIME.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .into(DeletedMessage.class);

        return new PageImpl<>(records);
    }
}
