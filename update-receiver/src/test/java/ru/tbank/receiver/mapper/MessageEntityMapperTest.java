package ru.tbank.receiver.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.tbank.common.telegram.MessageEntity;
import ru.tbank.common.telegram.enums.MessageEntityType;
import ru.tbank.receiver.exception.UnknownMessageEntityTypeException;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageEntityMapperTest {

    private final MessageEntityMapper messageEntityMapper = Mappers.getMapper(MessageEntityMapper.class);

    @Test
    void shouldMapSingleMessageEntity() {
        org.telegram.telegrambots.meta.api.objects.MessageEntity telegramEntity =
                new org.telegram.telegrambots.meta.api.objects.MessageEntity("custom_emoji", 0, 4);
        telegramEntity.setCustomEmojiId("emoji123");

        MessageEntity result = messageEntityMapper.toMessageEntity(telegramEntity);

        assertThat(result)
                .isNotNull()
                .satisfies(entity -> {
                    assertThat(entity.type()).isEqualTo(MessageEntityType.CUSTOM_EMOJI);
                    assertThat(entity.customEmojiId()).isEqualTo("emoji123");
                });
    }

    @Test
    void shouldMapMessageEntityList() {
        List<org.telegram.telegrambots.meta.api.objects.MessageEntity> telegramEntities = Arrays.asList(
                new org.telegram.telegrambots.meta.api.objects.MessageEntity("email", 0, 4),
                new org.telegram.telegrambots.meta.api.objects.MessageEntity("hashtag", 5, 6)
        );

        List<MessageEntity> result = messageEntityMapper.toMessageEntityList(telegramEntities);

        assertThat(result)
                .hasSize(2)
                .satisfiesExactly(
                        entity -> assertThat(entity.type()).isEqualTo(MessageEntityType.EMAIL),
                        entity -> assertThat(entity.type()).isEqualTo(MessageEntityType.HASHTAG)
                );
    }

    @Test
    void shouldThrowExceptionForUnknownEntityType() {
        org.telegram.telegrambots.meta.api.objects.MessageEntity unknownEntity =
                new org.telegram.telegrambots.meta.api.objects.MessageEntity("unknown", 0, 4);

        assertThatThrownBy(() -> messageEntityMapper.toMessageEntity(unknownEntity))
                .isInstanceOf(UnknownMessageEntityTypeException.class)
                .hasMessageContaining("Message entity with type 'unknown' is unknown");
    }
}