package ru.tbank.receiver.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.receiver.config.TelegramProperties;
import ru.tbank.receiver.service.UpdateSenderService;

@Slf4j
@Component
@RequiredArgsConstructor
public class Bot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramProperties telegramProperties;
    private final UpdateSenderService updateSenderService;

    @Override
    public String getBotToken() {
        return telegramProperties.token();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        log.debug("New update is: {}", update);
        updateSenderService.sendUpdate(update);
    }

    @AfterBotRegistration
    public void afterRegistration(@NonNull BotSession botSession) {
        log.info("Registered bot running state is: {}", botSession.isRunning());
    }
}
