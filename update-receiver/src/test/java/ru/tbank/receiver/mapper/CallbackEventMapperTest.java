package ru.tbank.receiver.mapper;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import ru.tbank.common.telegram.CallbackEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CallbackEventMapperTest {

    private final CallbackEventMapper callbackEventMapper = new CallbackEventMapperImpl(new UserMapperImpl());

    @Test
    void shouldMapCompleteCallbackEvent() {
        org.telegram.telegrambots.meta.api.objects.User telegramUser =
                new org.telegram.telegrambots.meta.api.objects.User(456L, "John", false);

        MaybeInaccessibleMessage message = mock(MaybeInaccessibleMessage.class);
        when(message.getMessageId()).thenReturn(123);

        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        when(callbackQuery.getId()).thenReturn("callback_id");
        when(callbackQuery.getData()).thenReturn("test_data");
        when(callbackQuery.getFrom()).thenReturn(telegramUser);
        when(callbackQuery.getMessage()).thenReturn(message);

        CallbackEvent result = callbackEventMapper.toCallbackEvent(callbackQuery);

        assertThat(result)
                .isNotNull()
                .satisfies(event -> {
                    assertThat(event.id()).isEqualTo("callback_id");
                    assertThat(event.messageId()).isEqualTo(123);
                    assertThat(event.data()).isEqualTo("test_data");
                    assertThat(event.user().id()).isEqualTo(456L);
                });
    }

    @Test
    void shouldMapCallbackEventWithMissingOptionalFields() {
        org.telegram.telegrambots.meta.api.objects.User telegramUser =
                new org.telegram.telegrambots.meta.api.objects.User(456L, "John", false);

        MaybeInaccessibleMessage message = mock(MaybeInaccessibleMessage.class);
        when(message.getMessageId()).thenReturn(null);

        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        when(callbackQuery.getId()).thenReturn("callback_id");
        when(callbackQuery.getData()).thenReturn(null);
        when(callbackQuery.getFrom()).thenReturn(telegramUser);
        when(callbackQuery.getMessage()).thenReturn(message);

        CallbackEvent result = callbackEventMapper.toCallbackEvent(callbackQuery);

        assertThat(result)
                .isNotNull()
                .satisfies(event -> {
                    assertThat(event.id()).isEqualTo("callback_id");
                    assertThat(event.messageId()).isNull();
                    assertThat(event.data()).isNull();
                    assertThat(event.user().id()).isEqualTo(456L);
                });
    }
}
