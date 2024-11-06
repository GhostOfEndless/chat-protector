package ru.tbank.processor.service.personal.handlers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.UserRole;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.utils.TelegramUtils;
import ru.tbank.processor.utils.UpdateType;

import java.util.List;

@Slf4j
@NullMarked
@RequiredArgsConstructor
public abstract class PersonalUpdateHandler {

    protected final AppUserService appUserService;
    protected final PersonalChatService personalChatService;
    protected final TelegramClientService telegramClientService;
    protected final TextResourceService textResourceService;

    @Getter
    protected final UserState processedUserState;

    public final void handle(UpdateType updateType, Update update, Long userId) {
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

    protected final void processUpdate(UpdateType updateType, Update update, AppUserRecord userRecord) {
        switch (updateType) {
            case PERSONAL_MESSAGE -> processTextMessageUpdate(update, userRecord);
            case CALLBACK -> processCallbackButtonUpdate(update.getCallbackQuery(), userRecord);
        }
    }

    protected void processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        telegramClientService.sendCallbackAnswer("Кнопка нажата", callbackQuery.getId(), false);
    }

    protected void processTextMessageUpdate(Update update, AppUserRecord userRecord) {
    }

    protected abstract MessagePayload buildMessagePayloadForUser(UserRole userRole);

    protected final void goToState(AppUserRecord userRecord, Integer messageId) {
        var userRole = UserRole.valueOf(userRecord.getRole());
        var messagePayload = buildMessagePayloadForUser(userRole);
        var keyboardMarkup = buildKeyboard(messagePayload.buttons(), userRecord.getLocale());

        try {
            if (messageId == 0) {
                Message sentMessage = telegramClientService.sendMessage(
                        userRecord.getId(),
                        textResourceService.getMessageText(messagePayload.messageText(), userRecord.getLocale()),
                        keyboardMarkup
                );
                personalChatService.save(userRecord.getId(), processedUserState.name(), sentMessage.getMessageId());
            } else {
                telegramClientService.editMessage(
                        userRecord.getId(),
                        messageId,
                        textResourceService.getMessageText(messagePayload.messageText(), userRecord.getLocale()),
                        keyboardMarkup
                );
                personalChatService.save(userRecord.getId(), processedUserState.name(), messageId);
            }
        } catch (TelegramApiException e) {
            log.error("Telegram API Error: {}", e.getMessage());
        }
    }

    protected final InlineKeyboardMarkup buildKeyboard(List<CallbackButtonPayload> callbackButtons, String userLocale) {
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

    protected final void removeMessageWithCallback(
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
