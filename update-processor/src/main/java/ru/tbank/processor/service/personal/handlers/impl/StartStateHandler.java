package ru.tbank.processor.service.personal.handlers.impl;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.UserState;
import ru.tbank.processor.service.personal.handlers.PersonalUpdateHandler;

@Slf4j
@Component
public class StartStateHandler extends PersonalUpdateHandler {

    public StartStateHandler(
            AppUserService appUserService,
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService
    ) {
        super(appUserService, personalChatService, telegramClientService, UserState.START);
    }

    @Override
    protected void processUpdate(@NonNull Update update, @NonNull AppUserRecord userRecord) {
        // TODO: временная реализация для проверки работоспособности хендлера
        if (update.getMessage().hasText() && update.getMessage().getText().equals("/start")) {
            try {
                Message sentMessage = telegramClientService.sendMessage(
                        userRecord.getId(),
                        "Привет, %s! Твоя роль: %s".formatted(userRecord.getFirstName(), userRecord.getRole())
                );
                personalChatService.save(userRecord.getId(), UserState.START.name(), sentMessage.getMessageId());
            } catch (TelegramApiException e) {
                log.error("Telegram API Error: {}", e.getMessage());
            }
        }
    }
}
