package ru.tbank.processor.service.personal;

import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.tbank.processor.config.TelegramProperties;
import ru.tbank.processor.excpetion.UserIdParsingException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.UpdateProcessingService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.common.entity.enums.UserRole;
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
    private final TelegramProperties telegramProperties;

    @PostConstruct
    public void init() {
        updateHandlers.forEach(handler ->
                updateHandlerMap.put(handler.getProcessedUserState(), handler)
        );
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
        var userRecord = appUserService.findById(userId)
                .orElseGet(() -> saveUser(user));

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

    private AppUserRecord saveUser(@NonNull User user) {
        log.info("User id is: {}, Owner id is: {}", user.getId(), telegramProperties.ownerId());
        if (user.getId().equals(telegramProperties.ownerId())) {
            log.info("Save OWNER");
            return appUserService.save(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUserName(),
                    UserRole.OWNER.name()
            );
        }

        log.info("Save USER");
        return appUserService.save(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserName()
        );
    }
}
