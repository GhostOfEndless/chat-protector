package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateReceiverService {

    private final PersonalUpdateProcessingService personalUpdateProcessingService;
    private final GroupChatUpdateProcessingService groupChatUpdateProcessingService;

    @KafkaListener(topics = "${kafka.updates-topic}", groupId = "update_consumer")
    public void listenUpdate(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().isGroupMessage()) {
                groupChatUpdateProcessingService.process(update);
            } else if (update.getMessage().isUserMessage()) {
                personalUpdateProcessingService.process(update);
            }
        } else {
            log.warn("Unknown update type! {}", update);
        }
    }
}
