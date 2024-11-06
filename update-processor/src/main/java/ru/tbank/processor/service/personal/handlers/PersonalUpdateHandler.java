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
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.UserRole;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;
import ru.tbank.processor.utils.UpdateType;

import java.util.List;

@Slf4j
@NullMarked
@RequiredArgsConstructor
public abstract class PersonalUpdateHandler {

    protected final PersonalChatService personalChatService;
    protected final TelegramClientService telegramClientService;
    protected final TextResourceService textResourceService;

    @Getter
    protected final UserState processedUserState;

    public final ProcessingResult handle(UpdateType updateType, Update update, AppUserRecord userRecord) {
        return processUpdate(updateType, update, userRecord);
    }

    public final void goToState(AppUserRecord userRecord, Integer messageId, Object[] args) {
        var userRole = UserRole.valueOf(userRecord.getRole());
        var messagePayload = buildMessagePayloadForUser(userRole, args);
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

    protected final ProcessingResult processUpdate(UpdateType updateType, Update update, AppUserRecord userRecord) {
        return switch (updateType) {
            case PERSONAL_MESSAGE -> processTextMessageUpdate(update, userRecord);
            case CALLBACK -> processCallbackButtonUpdate(update.getCallbackQuery(), userRecord);
            default -> new ProcessingResult(processedUserState, 0, new Object[]{});
        };
    }

    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        return new ProcessingResult(processedUserState, 0, new Object[]{});
    }

    protected ProcessingResult processTextMessageUpdate(Update update, AppUserRecord userRecord) {
        return new ProcessingResult(processedUserState, 0, new Object[]{});
    }

    protected abstract MessagePayload buildMessagePayloadForUser(UserRole userRole, Object[] args);

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

    protected final void showPermissionDeniedCallback(String userLocale, String callbackQueryId) {
        telegramClientService.sendCallbackAnswer(
                textResourceService.getCallbackText(CallbackTextCode.PERMISSION_DENIED, userLocale),
                callbackQueryId,
                true
        );
    }
}
