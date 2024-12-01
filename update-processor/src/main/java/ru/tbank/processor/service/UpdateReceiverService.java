package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.processor.utils.TelegramUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateReceiverService {

    private final UpdateProcessingService personalUpdateProcessingService;
    private final UpdateProcessingService groupChatUpdateProcessingService;

    @RabbitListener(queues = "${rabbit.updates-topic-name}")
    public void listenUpdates(@NonNull Update update) {
        processUpdate(update);
    }

    private void processUpdate(Update update) {
        var updateType = TelegramUtils.determineUpdateType(update);
        var chatType = TelegramUtils.determineChatType(updateType);

        switch (chatType) {
            case PERSONAL -> personalUpdateProcessingService.process(updateType, update);
            case GROUP -> groupChatUpdateProcessingService.process(updateType, update);
            case UNKNOWN -> log.warn("Unknown update type! {}", update);
        }
    }
}
