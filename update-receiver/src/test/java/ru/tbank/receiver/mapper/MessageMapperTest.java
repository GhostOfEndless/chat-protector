package ru.tbank.receiver.mapper;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import ru.tbank.common.telegram.Message;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MessageMapperTest {

    private final MessageMapper messageMapper = new MessageMapperImpl(
            new ChatMapperImpl(),
            new UserMapperImpl(),
            new MessageEntityMapperImpl()
    );

    @Test
    void shouldMapCompleteMessage() {
        org.telegram.telegrambots.meta.api.objects.message.Message telegramMessage =
                new org.telegram.telegrambots.meta.api.objects.message.Message();

        telegramMessage.setMessageId(123);
        telegramMessage.setText("Hello, world!");

        org.telegram.telegrambots.meta.api.objects.User telegramUser =
                new org.telegram.telegrambots.meta.api.objects.User(456L, "John", false);
        telegramMessage.setFrom(telegramUser);

        org.telegram.telegrambots.meta.api.objects.chat.Chat telegramChat =
                new org.telegram.telegrambots.meta.api.objects.chat.Chat(789L, "group");
        telegramChat.setTitle("Test Group");
        telegramMessage.setChat(telegramChat);

        List<MessageEntity> telegramEntities = List.of(new MessageEntity("hashtag", 0, 4));
        telegramMessage.setEntities(telegramEntities);

        Message result = messageMapper.toMessage(telegramMessage);

        assertThat(result)
                .isNotNull()
                .satisfies(message -> {
                    assertThat(message.messageId()).isEqualTo(123);
                    assertThat(message.text()).isEqualTo("Hello, world!");
                    assertThat(message.user().id()).isEqualTo(456L);
                    assertThat(message.chat().id()).isEqualTo(789L);
                    assertThat(message.entities()).hasSize(1);
                    assertThat(message.hasText()).isTrue();
                    assertThat(message.hasEntities()).isTrue();
                });
    }

    @Test
    void shouldMapMessageWithMissingOptionalFields() {
        org.telegram.telegrambots.meta.api.objects.message.Message telegramMessage =
                new org.telegram.telegrambots.meta.api.objects.message.Message();

        telegramMessage.setMessageId(123);
        telegramMessage.setText(null);

        Message result = messageMapper.toMessage(telegramMessage);

        assertThat(result)
                .isNotNull()
                .satisfies(message -> {
                    assertThat(message.messageId()).isEqualTo(123);
                    assertThat(message.text()).isNull();
                    assertThat(message.user()).isNull();
                    assertThat(message.chat()).isNull();
                    assertThat(message.entities()).isNull();
                    assertThat(message.hasText()).isFalse();
                    assertThat(message.hasEntities()).isFalse();
                });
    }
}
