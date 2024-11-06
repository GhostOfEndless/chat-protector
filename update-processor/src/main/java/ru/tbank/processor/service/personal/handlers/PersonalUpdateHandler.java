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
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.UserRole;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.MessagePayload;
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

    protected abstract void goToState(AppUserRecord userRecord, Integer messageId);

    protected abstract MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord);

    protected InlineKeyboardMarkup buildKeyboard(List<CallbackButtonPayload> callbackButtons, String userLocale) {
        var listOfRows = callbackButtons.stream()
                .map(callbackButton -> new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(textResourceService.getButtonText(callbackButton.text(), userLocale))
                                .callbackData(callbackButton.code())
                                .build())
                )
                .toList();

        return new InlineKeyboardMarkup(listOfRows);
    }

    protected boolean isRemovedExpiredMessage(
            AppUserRecord userRecord,
            String callbackQueryId,
            Integer chatLastMessageId,
            Integer callbackMessageId
    ) {
        if (!callbackMessageId.equals(chatLastMessageId)) {
            removeMessageWithCallback(
                    callbackMessageId,
                    userRecord,
                    CallbackTextCode.MESSAGE_EXPIRED,
                    callbackQueryId
            );
            return true;
        }
        return false;
    }

    protected void sendCallbackForPressedButton(
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
