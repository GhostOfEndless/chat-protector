package ru.tbank.receiver.service;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.receiver.config.KafkaProperties;

@Service
@RequiredArgsConstructor
public class UpdateSenderService {

    private final KafkaTemplate<String, Update> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    @Timed("telegramApiUpdate")
    public void sendUpdate(Update update) {
        kafkaTemplate.send(kafkaProperties.updatesTopic(), update);
    }
}
