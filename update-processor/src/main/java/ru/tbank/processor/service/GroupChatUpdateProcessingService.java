package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatUpdateProcessingService {

    private final ChatConfigService chatConfigService;

    public void process(Update update) {
        log.info("Group chat update: {}", update);
        var config = chatConfigService.getChatConfig(update.getMessage().getChatId());
        log.info("Config for this chat: {}", config);
    }
}
