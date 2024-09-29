package ru.tbank.receiver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class UpdateSenderService {

    private final KafkaTemplate<String, Update> kafkaTemplate;
    private final String updatesTopicName;

    public void sendUpdate(Update update) {
        kafkaTemplate.send(updatesTopicName, update);
    }
}
