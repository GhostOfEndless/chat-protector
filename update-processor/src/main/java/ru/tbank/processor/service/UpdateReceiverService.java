package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.processor.utils.TelegramUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateReceiverService {

    private final UpdateProcessingService personalUpdateProcessingService;
    private final UpdateProcessingService groupChatUpdateProcessingService;

    @KafkaListener(topics = "${kafka.updates-topic}", groupId = "update_consumer")
    public void listenUpdates(@NonNull Update update) {
        processUpdate(update);
    }

    private void processUpdate(Update update) {
        var updateType = TelegramUtils.determineUpdateType(update);

        switch (updateType) {
            case PERSONAL_MESSAGE, CALLBACK -> personalUpdateProcessingService.process(updateType, update);
            case GROUP_MESSAGE -> groupChatUpdateProcessingService.process(updateType, update);
            case UNKNOWN -> log.warn("Unknown update type! {}", update);
        }
    }
}
