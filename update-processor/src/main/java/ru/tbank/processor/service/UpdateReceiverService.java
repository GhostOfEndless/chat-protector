package ru.tbank.processor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
public class UpdateReceiverService {

    @KafkaListener(topics = "text-message-queue", groupId = "my_consumer")
    public void listenTextMessage(Update update) {
        log.info("Text message received: {}", update);
    }

    @KafkaListener(topics = "another-update-queue", groupId = "my_consumer")
    public void listenAnotherUpdate(Update update) {
        log.info("Another update received: {}", update);
    }
}
