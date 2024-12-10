package ru.tbank.admin.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
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

    public Page<DeletedMessage> getDeletedMessages(Long chatId, Long userId, @NonNull Pageable pageable) {
        var records = dslContext.selectFrom(table)
                .where(buildSearchCondition(chatId, userId))
                .orderBy(table.DELETION_TIME.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .into(DeletedMessage.class);

        return new PageImpl<>(records);
    }

    private Condition buildSearchCondition(Long chatId, Long userId) {
        if (userId > 0) {
            return table.CHAT_ID.eq(chatId).and(table.USER_ID.eq(userId));
        }
        return table.CHAT_ID.eq(chatId);
    }
}
