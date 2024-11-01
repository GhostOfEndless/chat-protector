package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.processor.service.group.GroupChatUpdateProcessingService;
import ru.tbank.processor.service.personal.PersonalUpdateProcessingService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateReceiverService {

    private final PersonalUpdateProcessingService personalUpdateProcessingService;
    private final GroupChatUpdateProcessingService groupChatUpdateProcessingService;

    @KafkaListener(topics = "${kafka.updates-topic}", groupId = "update_consumer")
    public void listenUpdate(@NonNull Update update) {
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
