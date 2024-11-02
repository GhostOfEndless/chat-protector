package ru.tbank.processor.service.personal;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.processor.service.personal.handlers.PersonalUpdateHandler;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalUpdateProcessingService {

    private final HashMap<UserState, PersonalUpdateHandler> updateHandlerMap = new HashMap<>();
    private final List<PersonalUpdateHandler> updateHandlers;
    private final PersonalChatService personalChatService;

    @PostConstruct
    public void init() {
        updateHandlers.forEach(handler -> updateHandlerMap.put(handler.getProcessedUserState(), handler));
    }

    public void process(@NonNull Update update) {
        Long userId = parseUserIdFromUpdate(update);

        if (userId != 0) {
            var personalChatRecord = personalChatService.findByUserId(userId);
            personalChatRecord.ifPresentOrElse(
                    it -> updateHandlerMap.get(UserState.valueOf(it.getState())).handle(update, userId),
                    () -> updateHandlerMap.get(UserState.START).handle(update, userId)
            );
        } else {
            log.warn("Unable to get user id from update!");
        }

        log.debug("Personal chat update: {}", update);
    }

    private @NonNull Long parseUserIdFromUpdate(@NonNull Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getId();
        } else {
            log.warn("Unsupported update type");
            return 0L;
        }
    }
}
