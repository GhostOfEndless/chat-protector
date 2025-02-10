package ru.tbank.receiver.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import ru.tbank.common.telegram.TelegramUpdate;
import ru.tbank.common.telegram.enums.UpdateType;
import ru.tbank.receiver.config.RabbitProperties;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSenderServiceTest {

    @Mock
    private RabbitProperties rabbitProperties;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UpdateSenderService updateSenderService;

    @Test
    void shouldSendGroupMessageToGroupQueue() {
        TelegramUpdate update = new TelegramUpdate(UpdateType.GROUP_MESSAGE, null, null, null);

        when(rabbitProperties.groupUpdatesQueueName()).thenReturn("group.updates");
        when(rabbitProperties.exchangeName()).thenReturn("telegram.exchange");

        updateSenderService.sendUpdate(update);

        verify(rabbitTemplate).convertAndSend(
                "telegram.exchange",
                "group.updates",
                update
        );
    }

    @Test
    void shouldSendGroupMemberEventToGroupQueue() {
        TelegramUpdate update = new TelegramUpdate(UpdateType.GROUP_MEMBER_EVENT, null, null, null);

        when(rabbitProperties.groupUpdatesQueueName()).thenReturn("group.updates");
        when(rabbitProperties.exchangeName()).thenReturn("telegram.exchange");

        updateSenderService.sendUpdate(update);

        verify(rabbitTemplate).convertAndSend(
                "telegram.exchange",
                "group.updates",
                update
        );
    }

    @Test
    void shouldSendPersonalMessageToPersonalQueue() {
        TelegramUpdate update = new TelegramUpdate(UpdateType.PERSONAL_MESSAGE, null, null, null);

        when(rabbitProperties.personalUpdatesQueueKey()).thenReturn("personal.updates");
        when(rabbitProperties.exchangeName()).thenReturn("telegram.exchange");

        updateSenderService.sendUpdate(update);

        verify(rabbitTemplate).convertAndSend(
                "telegram.exchange",
                "personal.updates",
                update
        );
    }

    @Test
    void shouldSendCallbackEventToPersonalQueue() {
        TelegramUpdate update = new TelegramUpdate(UpdateType.CALLBACK_EVENT, null, null, null);

        when(rabbitProperties.personalUpdatesQueueKey()).thenReturn("personal.updates");
        when(rabbitProperties.exchangeName()).thenReturn("telegram.exchange");

        updateSenderService.sendUpdate(update);

        verify(rabbitTemplate).convertAndSend(
                "telegram.exchange",
                "personal.updates",
                update
        );
    }
}