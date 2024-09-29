package ru.tbank.receiver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class UpdateSenderService {

    private final KafkaTemplate<String, Update> kafkaTemplate;

    public void sendTextMessageUpdate(Update update) {
        kafkaTemplate.send("text-message-queue", update);
    }

    public void sendAnotherUpdate(Update update) {
        kafkaTemplate.send("another-update-queue", update);
    }
}
