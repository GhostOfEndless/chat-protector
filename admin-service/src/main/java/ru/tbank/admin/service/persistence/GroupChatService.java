package ru.tbank.admin.service.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import ru.tbank.admin.exceptions.ChatNotFoundException;
import ru.tbank.admin.generated.tables.GroupChat;
import ru.tbank.admin.pojo.Chat;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final static GroupChat TABLE = GroupChat.GROUP_CHAT;
    private final DSLContext dslContext;

    public Chat getGroupChatById(Long chatId) {
        var groupChat = dslContext.fetchOptional(TABLE, TABLE.ID.eq(chatId))
                .orElseThrow(() -> new ChatNotFoundException(chatId));
        return groupChat.into(Chat.class);
    }

    public List<Chat> findAll() {
        return dslContext.selectFrom(TABLE)
                .fetch(r -> r.into(Chat.class));
    }

    public boolean existsById(Long chatId) {
        return dslContext.fetchOptional(TABLE, TABLE.ID.eq(chatId)).isPresent();
    }
}
