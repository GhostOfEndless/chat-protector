package ru.tbank.processor.service.personal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.processor.generated.tables.records.BotAppAdminRecord;

@Getter
@RequiredArgsConstructor
public abstract class PersonalUpdateHandler {

    protected final UserState userState;

    public void handle(@NonNull Update update, @NonNull BotAppAdminRecord botAppAdminRecord) {
        if (userState.matches(botAppAdminRecord.getState())) {
            processUpdate(update, botAppAdminRecord);
        }
    }

    protected abstract void processUpdate(@NonNull Update update, @NonNull BotAppAdminRecord botAppAdminRecord);
}
