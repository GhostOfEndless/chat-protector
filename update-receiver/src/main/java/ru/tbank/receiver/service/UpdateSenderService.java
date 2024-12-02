package ru.tbank.receiver.service;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.receiver.config.RabbitProperties;

@Service
@RequiredArgsConstructor
public class UpdateSenderService {

    private final RabbitProperties rabbitProperties;
    private final RabbitTemplate rabbitTemplate;

    @Timed("telegramApiUpdate")
    public void sendUpdate(Update update) {
        rabbitTemplate.convertAndSend(rabbitProperties.updatesTopicName(), update);
    }
}
