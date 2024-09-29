package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatUpdateProcessingService {

    private final ChatConfigService chatConfigService;

    public void process(Update update) {
        log.info("Group chat update: {}", update);
        var message = update.getMessage();
        var config = chatConfigService.getChatConfig(update.getMessage().getChatId());
        if (Objects.isNull(config)) {
            log.warn("Config is null! Creating ne config");
            chatConfigService.createChatConfig(message.getChatId(), message.getChat().getTitle());
            var createdConfig = chatConfigService.getChatConfig(message.getChatId());
            log.info("Created config is: {}", createdConfig);
        } else {
            log.info("Config for this chat: {}", config);
        }
    }
}
