package ru.tbank.processor.service.personal;

import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.common.telegram.TelegramUpdate;
import ru.tbank.common.telegram.User;
import ru.tbank.processor.config.TelegramProperties;
import ru.tbank.processor.excpetion.UnsupportedUpdateType;
import ru.tbank.processor.excpetion.UserIdParsingException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.handlers.PersonalUpdateHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalUpdateProcessingService {

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
    @RabbitListener(queues = "${rabbit.personal-updates-queue-name}")
    public void process(TelegramUpdate update) {
        User user = parseUserFromUpdate(update);
        if (user.id()== 0) {
            throw new UserIdParsingException("Unable to get user id from update!");
        }
        var personalChatRecord = personalChatService.findByUserId(user.id());
        var userRecord = appUserService.findById(user.id())
                .orElseGet(() -> saveNewUser(user));
        checkUserName(user, userRecord);
        personalChatRecord.ifPresentOrElse(
                it -> handleUpdate(update, userRecord, UserState.valueOf(it.getState())),
                () -> handleUpdate(update, userRecord, UserState.START)
        );
        log.debug("Personal chat update: {}", update);
    }

    private void handleUpdate(TelegramUpdate update, AppUserRecord userRecord, UserState userState) {
        var processingResult = updateHandlerMap.get(userState)
                .handle(update, userRecord);
        if (processingResult.newState() != userState || processingResult.newState() == UserState.START) {
            var handler = updateHandlerMap.get(processingResult.newState());
            if (handler != null) {
                handler.goToState(userRecord, processingResult.messageId(), processingResult.args());
            }
        }
    }

    private AppUserRecord saveNewUser(@NonNull User user) {
        log.info("User id is: {}, Owner id is: {}", user.id(), telegramProperties.ownerId());
        if (user.id().equals(telegramProperties.ownerId())) {
            log.info("Save OWNER");
            return appUserService.save(user, UserRole.OWNER.name());
        }
        log.info("Save USER");
        return appUserService.save(user);
    }

    private void checkUserName(@NonNull User user, @NonNull AppUserRecord userRecord) {
        String username = user.userName();
        if (!Objects.equals(username, userRecord.getUsername())
                && !(username == null && userRecord.getUsername().isBlank())) {
            log.info("Username for user with id={} updated to '{}'", user.id(), username);
            appUserService.updateUsername(user.id(), username);
            userRecord.setUsername(username);
        }
    }

    private User parseUserFromUpdate(@NonNull TelegramUpdate update) {
        return switch (update.updateType()) {
            case PERSONAL_MESSAGE -> update.message().user();
            case CALLBACK_EVENT -> update.callbackEvent().user();
            default -> throw new UnsupportedUpdateType("Update with this type is unsupported: %s".formatted(update));
        };
    }
}
