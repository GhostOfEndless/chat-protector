package ru.tbank.receiver.mapper;


import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.tbank.common.telegram.Chat;
import ru.tbank.common.telegram.enums.ChatType;
import ru.tbank.receiver.exception.UnknownChatTypeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatMapperTest {

    private final ChatMapper chatMapper = Mappers.getMapper(ChatMapper.class);

    @Test
    void shouldMapGroupChat() {
        org.telegram.telegrambots.meta.api.objects.chat.Chat telegramGroupChat =
                new org.telegram.telegrambots.meta.api.objects.chat.Chat(123L, "group");
        telegramGroupChat.setTitle("Test Group");

        Chat result = chatMapper.toGroupChat(telegramGroupChat);

        assertThat(result)
                .isNotNull()
                .satisfies(chat -> {
                    assertThat(chat.id()).isEqualTo(123L);
                    assertThat(chat.title()).isEqualTo("Test Group");
                    assertThat(chat.type()).isEqualTo(ChatType.GROUP);
                });
    }

    @Test
    void shouldMapPersonalChat() {
        org.telegram.telegrambots.meta.api.objects.chat.Chat telegramPersonalChat =
                new org.telegram.telegrambots.meta.api.objects.chat.Chat(456L, "private");
        telegramPersonalChat.setTitle(null);

        Chat result = chatMapper.toGroupChat(telegramPersonalChat);

        assertThat(result)
                .isNotNull()
                .satisfies(chat -> {
                    assertThat(chat.id()).isEqualTo(456L);
                    assertThat(chat.title()).isNull();
                    assertThat(chat.type()).isEqualTo(ChatType.PERSONAL);
                });
    }

    @Test
    void shouldThrowExceptionForUnknownChatType() {
        org.telegram.telegrambots.meta.api.objects.chat.Chat unknownChat =
                new org.telegram.telegrambots.meta.api.objects.chat.Chat(789L, "channel");

        assertThatThrownBy(() -> chatMapper.toGroupChat(unknownChat))
                .isInstanceOf(UnknownChatTypeException.class)
                .hasMessageContaining("Chat with type 'channel' is unknown!");
    }
}
