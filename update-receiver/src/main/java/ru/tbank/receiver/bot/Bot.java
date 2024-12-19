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
import ru.tbank.common.telegram.CallbackEvent;
import ru.tbank.common.telegram.GroupMemberEvent;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.TelegramUpdate;
import ru.tbank.common.telegram.enums.UpdateType;
import ru.tbank.receiver.config.TelegramProperties;
import ru.tbank.receiver.exception.UnsupportedUpdateTypeException;
import ru.tbank.receiver.service.UpdateSenderService;

@Slf4j
@Component
@RequiredArgsConstructor
public class Bot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramProperties telegramProperties;
    private final UpdateSenderService updateSenderService;
    private final UpdateParserService updateParserService;

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
        var telegramUpdate = buildTelegramUpdate(update);
        log.debug("Parsed update is: {}", telegramUpdate);
    }

    @AfterBotRegistration
    public void afterRegistration(@NonNull BotSession botSession) {
        log.info("Registered bot running state is: {}", botSession.isRunning());
    }

    private @NonNull TelegramUpdate buildTelegramUpdate(Update update) {
        UpdateType updateType = updateParserService.parseUpdateType(update);
        switch (updateType) {
            case MESSAGE -> {
                Message message = updateParserService.parseMessageFromUpdate(update);
                return TelegramUpdate.createMessageUpdate(message);
            }
            case CALLBACK_EVENT -> {
                CallbackEvent callbackEvent = updateParserService.parseCallbackEventFromUpdate(update);
                return TelegramUpdate.createCallbackEventUpdate(callbackEvent);
            }
            case GROUP_MEMBER_EVENT -> {
                GroupMemberEvent groupMemberEvent = updateParserService.parseGroupMemberEventFromUpdate(update);
                return TelegramUpdate.createGroupMemberEventUpdate(groupMemberEvent);
            }
            default -> throw new UnsupportedUpdateTypeException(
                    "Update type '%s' is not supported".formatted(updateType)
            );
        }
    }
}
