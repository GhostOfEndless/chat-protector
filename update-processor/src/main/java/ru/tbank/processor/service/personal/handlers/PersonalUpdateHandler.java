package ru.tbank.processor.service.personal.handlers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.AppUserService;
import ru.tbank.processor.service.personal.PersonalChatService;
import ru.tbank.processor.service.personal.UserRole;
import ru.tbank.processor.service.personal.UserState;

@RequiredArgsConstructor
public abstract class PersonalUpdateHandler {

    protected final AppUserService appUserService;
    protected final PersonalChatService personalChatService;
    protected final TelegramClientService telegramClientService;

    @Getter
    protected final UserState processedUserState;

    public void handle(@NonNull Update update, Long userId) {
        appUserService.findById(userId).ifPresent(user -> {
            if (UserRole.getRoleLevel(user.getRole()) >= processedUserState.getAllowedRoleLevel()) {
                processUpdate(update, user);
            }
        });
    }

    protected abstract void processUpdate(@NonNull Update update, @NonNull AppUserRecord userRecord);
}
