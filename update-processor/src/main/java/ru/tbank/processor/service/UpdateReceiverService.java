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
    public void listenUpdate(@NonNull Update update) {
        var updatePlace = TelegramUtils.determineUpdateType(update);

        switch (updatePlace) {
            case PERSONAL_MESSAGE, CALLBACK -> personalUpdateProcessingService.process(update);
            case GROUP_MESSAGE -> groupChatUpdateProcessingService.process(update);
            case UNKNOWN -> log.warn("Unknown update place! {}", update);
        }
    }
}
