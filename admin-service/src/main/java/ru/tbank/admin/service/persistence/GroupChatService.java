package ru.tbank.admin.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import ru.tbank.admin.pojo.Chat;
import ru.tbank.admin.exceptions.ChatNotFoundException;
import ru.tbank.admin.generated.tables.GroupChat;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final static GroupChat table = GroupChat.GROUP_CHAT;
    private final DSLContext dslContext;

    public Chat getGroupChatById(Long chatId) {
        var groupChat = dslContext.fetchOptional(table, table.ID.eq(chatId))
                .orElseThrow(() -> new ChatNotFoundException(chatId));
        return groupChat.into(Chat.class);
    }

    public List<Chat> findAll() {
        return dslContext.selectFrom(table)
                .fetch(r -> r.into(Chat.class));
    }

    public boolean existsById(Long chatId) {
        return dslContext.fetchOptional(table, table.ID.eq(chatId)).isPresent();
    }
}
