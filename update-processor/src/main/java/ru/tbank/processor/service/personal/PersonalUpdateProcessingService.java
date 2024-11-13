package ru.tbank.processor.service.personal;

import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.processor.excpetion.UserIdParsingException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.UpdateProcessingService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.handlers.PersonalUpdateHandler;
import ru.tbank.processor.utils.TelegramUtils;
import ru.tbank.processor.utils.enums.UpdateType;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalUpdateProcessingService implements UpdateProcessingService {

    protected final AppUserService appUserService;
    private final HashMap<UserState, PersonalUpdateHandler> updateHandlerMap = new HashMap<>();
    private final List<PersonalUpdateHandler> updateHandlers;
    private final PersonalChatService personalChatService;

    @PostConstruct
    public void init() {
        updateHandlers.forEach(handler -> updateHandlerMap.put(handler.getProcessedUserState(), handler));
    }

    @Timed("personalMessageProcessing")
    @Override
    public void process(UpdateType updateType, Update update) {
        Long userId = TelegramUtils.getUserFromUpdate(update).getId();

        if (userId == 0) {
            throw new UserIdParsingException("Unable to get user id from update!");
        }

        var personalChatRecord = personalChatService.findByUserId(userId);
        var user = TelegramUtils.getUserFromUpdate(update);
        var userRecord = appUserService.findById(userId).orElseGet(
                () -> appUserService.saveRegularUser(
                        userId,
                        user.getFirstName(),
                        user.getLastName(),
                        user.getUserName()
                ));

        personalChatRecord.ifPresentOrElse(
                it -> handleUpdate(updateType, update, userRecord, UserState.valueOf(it.getState())),
                () -> handleUpdate(updateType, update, userRecord, UserState.START)
        );

        log.debug("Personal chat update: {}", update);
    }

    private void handleUpdate(UpdateType updateType, Update update, AppUserRecord userRecord, UserState userState) {
        var processingResult = updateHandlerMap.get(userState)
                .handle(updateType, update, userRecord);

        if (processingResult.newState() != userState || processingResult.newState() == UserState.START) {
            var handler = updateHandlerMap.get(processingResult.newState());
            if (handler != null) {
                handler.goToState(userRecord, processingResult.messageId(), processingResult.args());
            }
        }
    }
}
