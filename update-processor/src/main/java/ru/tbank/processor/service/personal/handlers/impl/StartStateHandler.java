package ru.tbank.processor.service.personal.handlers.impl;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.AppUserService;
import ru.tbank.processor.service.personal.PersonalChatService;
import ru.tbank.processor.service.personal.UserState;
import ru.tbank.processor.service.personal.handlers.PersonalUpdateHandler;

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
        // TODO: тут мы должны отправить ответ на команду start
    }
}
