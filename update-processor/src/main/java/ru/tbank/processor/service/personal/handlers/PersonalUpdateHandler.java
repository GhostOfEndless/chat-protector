package ru.tbank.processor.service.personal.handlers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserRole;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackAnswerPayload;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;
import ru.tbank.processor.utils.TelegramUtils;
import ru.tbank.processor.utils.enums.UpdateType;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Slf4j
@NullMarked
@RequiredArgsConstructor
public abstract class PersonalUpdateHandler {

    protected final PersonalChatService personalChatService;
    protected final TelegramClientService telegramClientService;
    protected final TextResourceService textResourceService;

    @Getter
    protected final UserState processedUserState;

    protected final Supplier<MessagePayload> chatNotFoundMessage = () -> MessagePayload.create(
            MessageTextCode.CHAT_MESSAGE_NOT_FOUND,
            List.of(CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK))
    );

    public final ProcessingResult handle(UpdateType updateType, Update update, AppUserRecord userRecord) {
        return processUpdate(updateType, update, userRecord);
    }

    public final void goToState(AppUserRecord userRecord, Integer messageId, Object... args) {
        var messagePayload = buildMessagePayloadForUser(userRecord, args);
        var keyboardMarkup = buildKeyboard(messagePayload.buttons(), userRecord.getLocale());
        String messageText = buildMessageText(userRecord.getLocale(), messagePayload);
        Long userId = userRecord.getId();

        try {
            if (messageId == 0) {
                var sentMessage = telegramClientService.sendMessage(userId, messageText, keyboardMarkup);
                personalChatService.save(userId, processedUserState.name(), sentMessage.getMessageId());
            } else {
                telegramClientService.editMessage(userId, messageId, messageText, keyboardMarkup);
                personalChatService.save(userId, processedUserState.name(), messageId);
            }
        } catch (TelegramApiException e) {
            log.error("Telegram API Error: {}", e.getMessage());
        }
    }

    private String buildMessageText(String userLocale, MessagePayload messagePayload) {
        var messageArgs = messagePayload.messageArgs()
                .stream()
                .map(argument -> argument.isResource()
                        ? textResourceService.getText(argument.text(), userLocale)
                        : argument.text()
                )
                .toArray();
        return textResourceService.getMessageText(
                messagePayload.messageText(),
                messageArgs,
                userLocale
        );
    }

    protected final ProcessingResult processUpdate(UpdateType updateType, Update update, AppUserRecord userRecord) {
        return switch (updateType) {
            case PERSONAL_MESSAGE -> processTextMessageUpdate(update, userRecord);
            case CALLBACK -> processCallbackUpdate(update.getCallbackQuery(), userRecord);
            default -> ProcessingResult.create(processedUserState);
        };
    }

    private ProcessingResult processCallbackUpdate(
            CallbackQuery callbackQuery,
            AppUserRecord userRecord
    ) {
        var callbackMessageId = callbackQuery.getMessage().getMessageId();
        Integer lastMessageId = personalChatService.findByUserId(userRecord.getId())
                .orElseThrow()
                .getLastMessageId();

        if (!Objects.equals(callbackMessageId, lastMessageId)) {
            showMessageExpiredCallback(userRecord.getLocale(), callbackQuery.getId());
            return ProcessingResult.create(UserState.NONE);
        }
        var callbackData = TelegramUtils.parseCallbackData(callbackQuery);
        return processCallbackButtonUpdate(callbackData, userRecord);
    }

    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        return ProcessingResult.create(processedUserState);
    }

    protected ProcessingResult processTextMessageUpdate(Update update, AppUserRecord userRecord) {
        if (update.getMessage().hasText() && update.getMessage().getText().startsWith("/start")) {
            return ProcessingResult.create(UserState.START);
        }
        return ProcessingResult.create(processedUserState);
    }

    protected abstract MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args);

    protected final ProcessingResult checkPermissionAndProcess(
            UserRole requiredRole,
            AppUserRecord userRecord,
            Supplier<ProcessingResult> supplier,
            CallbackData callbackData
    ) {
        UserRole userRole = UserRole.getRoleByName(userRecord.getRole());
        if (!requiredRole.isEqualOrLowerThan(userRole)) {
            showPermissionDeniedCallback(userRecord.getLocale(), callbackData.callbackId());
            return ProcessingResult.create(UserState.START, callbackData.messageId());
        }
        return supplier.get();
    }

    protected final InlineKeyboardMarkup buildKeyboard(List<CallbackButtonPayload> callbackButtons, String userLocale) {
        var listOfRows = callbackButtons.stream()
                .map(callbackButton -> {
                            String buttonText = textResourceService.getText(callbackButton.text(), userLocale);
                            var inlineKeyboardButton = new InlineKeyboardButton(buttonText);
                            var inlineKeyboardRow = new InlineKeyboardRow(inlineKeyboardButton);

                            if (callbackButton.isUrl()) {
                                inlineKeyboardButton.setUrl(callbackButton.code());
                            } else {
                                inlineKeyboardButton.setCallbackData(callbackButton.code());
                            }

                            return inlineKeyboardRow;
                        }
                )
                .toList();
        return new InlineKeyboardMarkup(listOfRows);
    }

    protected final void showAnswerCallback(
            CallbackAnswerPayload callbackAnswerPayload,
            String userLocale,
            String callbackQueryId
    ) {
        showAnswerCallback(callbackAnswerPayload, userLocale, callbackQueryId, false);
    }

    protected final void showAnswerCallback(
            CallbackAnswerPayload callbackAnswerPayload,
            String userLocale,
            String callbackQueryId,
            boolean isAlert
    ) {
        var callbackArgs = callbackAnswerPayload.callbackArgs()
                .stream()
                .map(argument -> argument.isResource()
                        ? textResourceService.getText(argument.text(), userLocale)
                        : argument.text()
                )
                .toArray();

        telegramClientService.sendCallbackAnswer(
                textResourceService.getCallbackText(callbackAnswerPayload.callbackText(), callbackArgs, userLocale),
                callbackQueryId,
                isAlert
        );
    }

    protected final void showChatUnavailableCallback(String callbackId, String userLocale) {
        showAnswerCallback(
                CallbackAnswerPayload.create(
                        CallbackTextCode.CHAT_UNAVAILABLE
                ),
                userLocale,
                callbackId
        );
    }

    private void showMessageExpiredCallback(String userLocale, String callbackId) {
        showAnswerCallback(
                CallbackAnswerPayload.create(
                        CallbackTextCode.MESSAGE_EXPIRED
                ),
                userLocale,
                callbackId,
                true
        );
    }

    private void showPermissionDeniedCallback(String userLocale, String callbackId) {
        showAnswerCallback(
                CallbackAnswerPayload.create(
                        CallbackTextCode.PERMISSION_DENIED
                ),
                userLocale,
                callbackId,
                true
        );
    }
}
