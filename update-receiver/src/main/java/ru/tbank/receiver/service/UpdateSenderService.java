package ru.tbank.receiver.service;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ru.tbank.common.telegram.TelegramUpdate;
import ru.tbank.receiver.config.RabbitProperties;

@Service
@RequiredArgsConstructor
public class UpdateSenderService {

    private final RabbitProperties rabbitProperties;
    private final RabbitTemplate rabbitTemplate;

    @Timed("telegramApiUpdate")
    public void sendUpdate(@NonNull TelegramUpdate update) {
        String routingKey = switch (update.updateType()) {
            case GROUP_MESSAGE, GROUP_MEMBER_EVENT -> rabbitProperties.groupUpdatesQueueKey();
            case CALLBACK_EVENT, PERSONAL_MESSAGE -> rabbitProperties.personalUpdatesQueueKey();
        };
        rabbitTemplate.convertAndSend(rabbitProperties.exchangeName(), routingKey, update);
    }
}
