package ru.tbank.receiver.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberUpdated;
import ru.tbank.common.telegram.CallbackEvent;
import ru.tbank.common.telegram.GroupMemberEvent;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.enums.UpdateType;
import ru.tbank.receiver.exception.UnknownUpdateTypeException;
import ru.tbank.receiver.mapper.CallbackEventMapper;
import ru.tbank.receiver.mapper.GroupMemberEventMapper;
import ru.tbank.receiver.mapper.MessageMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateParserServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private CallbackEventMapper callbackEventMapper;

    @Mock
    private GroupMemberEventMapper groupMemberEventMapper;

    @InjectMocks
    private UpdateParserService updateParserService;

    @Test
    void shouldParsePersonalMessageUpdateType() {
        Update update = mock(Update.class);
        org.telegram.telegrambots.meta.api.objects.message.Message message =
                mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        org.telegram.telegrambots.meta.api.objects.chat.Chat chat = mock(Chat.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChat()).thenReturn(chat);
        when(chat.isUserChat()).thenReturn(true);

        UpdateType result = updateParserService.parseUpdateType(update);

        assertThat(result).isEqualTo(UpdateType.PERSONAL_MESSAGE);
    }

    @Test
    void shouldParseGroupMessageUpdateType() {
        Update update = mock(Update.class);
        org.telegram.telegrambots.meta.api.objects.message.Message message =
                mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        org.telegram.telegrambots.meta.api.objects.chat.Chat chat = mock(Chat.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChat()).thenReturn(chat);
        when(chat.isGroupChat()).thenReturn(true);

        UpdateType result = updateParserService.parseUpdateType(update);

        assertThat(result).isEqualTo(UpdateType.GROUP_MESSAGE);
    }

    @Test
    void shouldParseGroupMemberEventUpdateType() {
        Update update = mock(Update.class);
        org.telegram.telegrambots.meta.api.objects.chat.Chat chat = mock(Chat.class);
        ChatMemberUpdated myChatMember = mock(ChatMemberUpdated.class);

        when(update.hasMyChatMember()).thenReturn(true);
        when(update.getMyChatMember()).thenReturn(myChatMember);
        when(myChatMember.getChat()).thenReturn(chat);
        when(chat.isGroupChat()).thenReturn(true);

        UpdateType result = updateParserService.parseUpdateType(update);

        assertThat(result).isEqualTo(UpdateType.GROUP_MEMBER_EVENT);
    }

    @Test
    void shouldParseCallbackEventUpdateType() {
        Update update = mock(Update.class);

        when(update.hasCallbackQuery()).thenReturn(true);

        UpdateType result = updateParserService.parseUpdateType(update);

        assertThat(result).isEqualTo(UpdateType.CALLBACK_EVENT);
    }

    @Test
    void shouldThrowExceptionForUnknownUpdateType() {
        Update update = mock(Update.class);

        assertThatThrownBy(() -> updateParserService.parseUpdateType(update))
                .isInstanceOf(UnknownUpdateTypeException.class);
    }

    @Test
    void shouldParseMessageFromUpdate() {
        Update update = mock(Update.class);
        org.telegram.telegrambots.meta.api.objects.message.Message telegramMessage = mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        Message mappedMessage = mock(Message.class);

        when(update.getMessage()).thenReturn(telegramMessage);
        when(messageMapper.toMessage(telegramMessage)).thenReturn(mappedMessage);

        Message result = updateParserService.parseMessageFromUpdate(update);

        assertThat(result).isEqualTo(mappedMessage);
        verify(messageMapper).toMessage(telegramMessage);
    }

    @Test
    void shouldParseGroupMemberEventFromUpdate() {
        Update update = mock(Update.class);
        ChatMemberUpdated chatMemberUpdated = mock(ChatMemberUpdated.class);
        GroupMemberEvent mappedEvent = mock(GroupMemberEvent.class);

        when(update.getMyChatMember()).thenReturn(chatMemberUpdated);
        when(groupMemberEventMapper.toGroupMemberEvent(chatMemberUpdated)).thenReturn(mappedEvent);

        GroupMemberEvent result = updateParserService.parseGroupMemberEventFromUpdate(update);

        assertThat(result).isEqualTo(mappedEvent);
        verify(groupMemberEventMapper).toGroupMemberEvent(chatMemberUpdated);
    }

    @Test
    void shouldParseCallbackEventFromUpdate() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        CallbackEvent mappedEvent = mock(CallbackEvent.class);

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackEventMapper.toCallbackEvent(callbackQuery)).thenReturn(mappedEvent);

        CallbackEvent result = updateParserService.parseCallbackEventFromUpdate(update);

        assertThat(result).isEqualTo(mappedEvent);
        verify(callbackEventMapper).toCallbackEvent(callbackQuery);
    }
}
