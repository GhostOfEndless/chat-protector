package ru.tbank.processor.service.moderation;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.tbank.processor.entity.ChatUser;

@Service
public class ChatUsersService {

    private static final String KEY_TEMPLATE = "chat:%d:user:%d";

    private final RedisTemplate<String, ChatUser> redisChatUsersTemplate;

    public ChatUsersService(
            @Qualifier("redisChatUsersTemplate")
            RedisTemplate<String, ChatUser> redisChatUsersTemplate
    ) {
        this.redisChatUsersTemplate = redisChatUsersTemplate;
    }

    public Optional<ChatUser> findChatUser(Long chatId, Long userId) {
        return Optional.ofNullable(redisChatUsersTemplate.opsForValue()
                .get(KEY_TEMPLATE.formatted(chatId, userId)));
    }

    public void updateChatUser(ChatUser chatUser) {
        redisChatUsersTemplate.opsForValue()
                .set(KEY_TEMPLATE.formatted(chatUser.getChatId(), chatUser.getUserId()), chatUser);
    }
}
