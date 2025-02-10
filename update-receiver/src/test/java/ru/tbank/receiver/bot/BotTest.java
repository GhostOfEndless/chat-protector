package ru.tbank.receiver.bot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.common.telegram.CallbackEvent;
import ru.tbank.common.telegram.GroupMemberEvent;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.TelegramUpdate;
import ru.tbank.common.telegram.enums.UpdateType;
import ru.tbank.receiver.service.UpdateParserService;
import ru.tbank.receiver.service.UpdateSenderService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotTest {

    @Mock
    private UpdateSenderService updateSenderService;

    @Mock
    private UpdateParserService updateParserService;

    @InjectMocks
    private Bot bot;

    @Test
    void shouldProcessPersonalMessageUpdate() {
        Update update = mock(Update.class);
        Message parsedMessage = mock(Message.class);

        when(updateParserService.parseUpdateType(update)).thenReturn(UpdateType.PERSONAL_MESSAGE);
        when(updateParserService.parseMessageFromUpdate(update)).thenReturn(parsedMessage);

        bot.consume(update);

        verify(updateSenderService).sendUpdate(any(TelegramUpdate.class));
    }

    @Test
    void shouldProcessGroupMessageUpdate() {
        Update update = mock(Update.class);
        Message parsedMessage = mock(Message.class);

        when(updateParserService.parseUpdateType(update)).thenReturn(UpdateType.GROUP_MESSAGE);
        when(updateParserService.parseMessageFromUpdate(update)).thenReturn(parsedMessage);

        bot.consume(update);

        verify(updateSenderService).sendUpdate(any(TelegramUpdate.class));
    }

    @Test
    void shouldProcessCallbackEventUpdate() {
        Update update = mock(Update.class);
        CallbackEvent parsedCallbackEvent = mock(CallbackEvent.class);

        when(updateParserService.parseUpdateType(update)).thenReturn(UpdateType.CALLBACK_EVENT);
        when(updateParserService.parseCallbackEventFromUpdate(update)).thenReturn(parsedCallbackEvent);

        bot.consume(update);

        verify(updateSenderService).sendUpdate(any(TelegramUpdate.class));
    }

    @Test
    void shouldProcessGroupMemberEventUpdate() {
        Update update = mock(Update.class);
        GroupMemberEvent parsedGroupMemberEvent = mock(GroupMemberEvent.class);

        when(updateParserService.parseUpdateType(update)).thenReturn(UpdateType.GROUP_MEMBER_EVENT);
        when(updateParserService.parseGroupMemberEventFromUpdate(update)).thenReturn(parsedGroupMemberEvent);

        bot.consume(update);

        verify(updateSenderService).sendUpdate(any(TelegramUpdate.class));
    }
}