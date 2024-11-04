package ru.tbank.processor.service.personal.handlers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.UserRole;
import ru.tbank.processor.service.personal.UserState;
import ru.tbank.processor.service.personal.handlers.impl.ButtonTextCode;
import ru.tbank.processor.service.personal.handlers.impl.CallbackTextCode;
import ru.tbank.processor.utils.TelegramUtils;
import ru.tbank.processor.utils.UpdateType;

import java.util.List;

@NullMarked
@RequiredArgsConstructor
public abstract class PersonalUpdateHandler {

    protected final AppUserService appUserService;
    protected final PersonalChatService personalChatService;
    protected final TelegramClientService telegramClientService;
    protected final TextResourceService textResourceService;

    @Getter
    protected final UserState processedUserState;

    public void handle(UpdateType updateType, Update update, Long userId) {
        var user = TelegramUtils.getUserFromUpdate(update);
        var userRecord = appUserService.findById(userId).orElseGet(
                () -> appUserService.saveRegularUser(
                        userId,
                        user.getFirstName(),
                        user.getLastName(),
                        user.getUserName()
                ));

        if (UserRole.getRoleLevel(userRecord.getRole()) >= processedUserState.getAllowedRoleLevel()) {
            processUpdate(updateType, update, userRecord);
        }
    }

    protected abstract void processUpdate(UpdateType updateType, Update update, AppUserRecord userRecord);

    protected InlineKeyboardMarkup buildKeyboard(List<ButtonTextCode> buttonsText, String userLocale) {
        var listOfRows = buttonsText.stream()
                .map(textCode -> new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(textResourceService.getButtonText(textCode, userLocale))
                                .callbackData(textCode.name())
                                .build()))
                .toList();

        return new InlineKeyboardMarkup(listOfRows);
    }

    protected void sendCallback(
            CallbackTextCode callbackTextCode,
            String callbackQueryId,
            ButtonTextCode pressedButton,
            String userLocale
    ) {
        telegramClientService.sendCallbackAnswer(
                textResourceService.getCallbackText(
                        callbackTextCode,
                        new Object[]{textResourceService.getButtonText(pressedButton, userLocale)},
                        userLocale
                ),
                callbackQueryId,
                false
        );
    }

    protected void removeMessageWithCallback(
            Integer messageId,
            AppUserRecord userRecord,
            CallbackTextCode callbackTextCode,
            String callbackQueryId
    ) {
        telegramClientService.sendCallbackAnswer(
                textResourceService.getCallbackText(callbackTextCode, userRecord.getLocale()),
                callbackQueryId,
                true
        );
        telegramClientService.deleteMessage(userRecord.getId(), messageId);
    }
}
