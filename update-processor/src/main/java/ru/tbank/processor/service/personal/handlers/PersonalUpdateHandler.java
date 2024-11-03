package ru.tbank.processor.service.personal.handlers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.UserRole;
import ru.tbank.processor.service.personal.UserState;
import ru.tbank.processor.utils.TelegramUtils;

@RequiredArgsConstructor
public abstract class PersonalUpdateHandler {

    protected final AppUserService appUserService;
    protected final PersonalChatService personalChatService;
    protected final TelegramClientService telegramClientService;
    protected final TextResourceService textResourceService;

    @Getter
    protected final UserState processedUserState;

    public void handle(@NonNull Update update, Long userId) {
        var user = TelegramUtils.getUserFromUpdate(update);
        var userRecord = appUserService.findById(userId).orElseGet(
                () -> appUserService.saveRegularUser(
                        userId,
                        user.getFirstName(),
                        user.getLastName(),
                        user.getUserName()
                ));

        if (UserRole.getRoleLevel(userRecord.getRole()) >= processedUserState.getAllowedRoleLevel()) {
            processUpdate(update, userRecord);
        }
    }

    protected abstract void processUpdate(@NonNull Update update, @NonNull AppUserRecord userRecord);
}
