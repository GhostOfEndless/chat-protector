package ru.tbank.receiver.mapper;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberUpdated;
import ru.tbank.common.telegram.GroupMemberEvent;
import ru.tbank.common.telegram.enums.GroupMemberEventType;
import ru.tbank.receiver.exception.UnknownGroupMemberEventTypeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupMemberEventMapperTest {

    private final GroupMemberEventMapper groupMemberEventMapper = new GroupMemberEventMapperImpl(
            new ChatMapperImpl(),
            new UserMapperImpl()
    );

    @Test
    void shouldMapBotAddedEvent() {
        ChatMemberUpdated chatMemberUpdated = createChatMemberUpdated(
                createChatMember("left"),
                createChatMember("member"),
                createUser(true)
        );

        GroupMemberEvent result = groupMemberEventMapper.toGroupMemberEvent(chatMemberUpdated);

        assertThat(result.eventType()).isEqualTo(GroupMemberEventType.GROUP_BOT_ADDED);
    }

    @Test
    void shouldMapBotLeftEvent() {
        ChatMemberUpdated chatMemberUpdated = createChatMemberUpdated(
                createChatMember("member"),
                createChatMember("left"),
                createUser(true)
        );

        GroupMemberEvent result = groupMemberEventMapper.toGroupMemberEvent(chatMemberUpdated);

        assertThat(result.eventType()).isEqualTo(GroupMemberEventType.GROUP_BOT_LEFT);
    }

    @Test
    void shouldMapBotKickedEvent() {
        ChatMemberUpdated chatMemberUpdated = createChatMemberUpdated(
                createChatMember("member"),
                createChatMember("kicked"),
                createUser(false)
        );

        GroupMemberEvent result = groupMemberEventMapper.toGroupMemberEvent(chatMemberUpdated);

        assertThat(result.eventType()).isEqualTo(GroupMemberEventType.GROUP_BOT_KICKED);
    }

    @Test
    void shouldThrowExceptionForUnknownEventType() {
        ChatMemberUpdated chatMemberUpdated = createChatMemberUpdated(
                createChatMember("member"),
                createChatMember("member"),
                createUser(false)
        );

        assertThatThrownBy(() -> groupMemberEventMapper.toGroupMemberEvent(chatMemberUpdated))
                .isInstanceOf(UnknownGroupMemberEventTypeException.class);
    }

    private ChatMemberUpdated createChatMemberUpdated(
            ChatMember oldChatMember,
            ChatMember newChatMember,
            org.telegram.telegrambots.meta.api.objects.User from
    ) {
        ChatMemberUpdated chatMemberUpdated = mock(ChatMemberUpdated.class);
        when(chatMemberUpdated.getOldChatMember()).thenReturn(oldChatMember);
        when(chatMemberUpdated.getNewChatMember()).thenReturn(newChatMember);
        when(chatMemberUpdated.getFrom()).thenReturn(from);
        return chatMemberUpdated;
    }

    private ChatMember createChatMember(String status) {
        ChatMember chatMember = mock(ChatMember.class);
        when(chatMember.getStatus()).thenReturn(status);
        return chatMember;
    }

    private org.telegram.telegrambots.meta.api.objects.User createUser(boolean isBot) {
        org.telegram.telegrambots.meta.api.objects.User user =
                new org.telegram.telegrambots.meta.api.objects.User(123L, "Test", isBot);
        user.setLastName("User");
        return user;
    }
}
