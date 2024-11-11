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
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;
import ru.tbank.processor.utils.UpdateType;

import java.util.List;
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

    public final void goToState(AppUserRecord userRecord, Integer messageId, Object[] args) {
        var userRole = UserRole.valueOf(userRecord.getRole());
        var messagePayload = buildMessagePayloadForUser(userRole, args);
        var keyboardMarkup = buildKeyboard(messagePayload.buttons(), userRecord.getLocale());

        var messageArgs = messagePayload.messageArgs()
                .stream()
                .map(argument -> argument.isResource()
                        ? textResourceService.getText(argument.text(), userRecord.getLocale())
                        : argument.text()
                )
                .toArray();
        var messageText = textResourceService.getMessageText(
                messagePayload.messageText(),
                messageArgs,
                userRecord.getLocale()
        );

        try {
            if (messageId == 0) {
                var sentMessage = telegramClientService.sendMessage(userRecord.getId(), messageText, keyboardMarkup);
                personalChatService.save(userRecord.getId(), processedUserState.name(), sentMessage.getMessageId());
            } else {
                telegramClientService.editMessage(userRecord.getId(), messageId, messageText, keyboardMarkup);
                personalChatService.save(userRecord.getId(), processedUserState.name(), messageId);
            }
        } catch (TelegramApiException e) {
            log.error("Telegram API Error: {}", e.getMessage());
        }
    }

    protected final ProcessingResult processUpdate(UpdateType updateType, Update update, AppUserRecord userRecord) {
        int lastMessageId = personalChatService.findByUserId(userRecord.getId())
                .orElseThrow()
                .getLastMessageId();

        return switch (updateType) {
            case PERSONAL_MESSAGE -> processTextMessageUpdate(update, userRecord);
            case CALLBACK -> {
                if (update.getCallbackQuery().getMessage().getMessageId() != lastMessageId) {
                    showAlertCallback(
                            CallbackTextCode.MESSAGE_EXPIRED,
                            userRecord.getLocale(),
                            update.getCallbackQuery().getId()
                    );
                    yield new ProcessingResult(processedUserState, 0, new Object[]{});
                }
                yield processCallbackButtonUpdate(update.getCallbackQuery(), userRecord);
            }
            default -> new ProcessingResult(processedUserState, 0, new Object[]{});
        };
    }

    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        return new ProcessingResult(processedUserState, 0, new Object[]{});
    }

    protected ProcessingResult processTextMessageUpdate(Update update, AppUserRecord userRecord) {
        if (update.getMessage().hasText() && update.getMessage().getText().startsWith("/start")) {
            return new ProcessingResult(UserState.START, 0, new Object[]{});
        }

        return new ProcessingResult(processedUserState, 0, new Object[]{});
    }

    protected abstract MessagePayload buildMessagePayloadForUser(UserRole userRole, Object[] args);

    protected final ProcessingResult checkPermissionAndProcess(
            UserRole requiredRole,
            AppUserRecord userRecord,
            Supplier<ProcessingResult> supplier,
            CallbackQuery callbackQuery
    ) {
        return checkPermissionAndProcess(requiredRole, userRecord, supplier, new Object[]{}, callbackQuery);
    }

    protected final ProcessingResult checkPermissionAndProcess(
            UserRole requiredRole,
            AppUserRecord userRecord,
            Supplier<ProcessingResult> supplier,
            Object[] args,
            CallbackQuery callbackQuery
    ) {
        UserRole userRole = UserRole.valueOf(userRecord.getRole());
        if (!requiredRole.isEqualOrLowerThan(userRole)) {
            showAlertCallback(CallbackTextCode.PERMISSION_DENIED, userRecord.getLocale(), callbackQuery.getId());
            return new ProcessingResult(processedUserState, callbackQuery.getMessage().getMessageId(), args);
        }

        return supplier.get();
    }

    protected final InlineKeyboardMarkup buildKeyboard(List<CallbackButtonPayload> callbackButtons, String userLocale) {
        var listOfRows = callbackButtons.stream()
                .map(callbackButton -> new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(textResourceService.getText(callbackButton.text(), userLocale))
                                .callbackData(callbackButton.code())
                                .build())
                )
                .toList();

        return new InlineKeyboardMarkup(listOfRows);
    }

    protected final void showRegularCallback(
            CallbackTextCode callbackTextCode,
            String userLocale,
            String callbackQueryId
    ) {
        telegramClientService.sendCallbackAnswer(
                textResourceService.getCallbackText(callbackTextCode, userLocale),
                callbackQueryId,
                false
        );
    }

    protected final void showAlertCallback(
            CallbackTextCode callbackTextCode,
            String userLocale,
            String callbackQueryId
    ) {
        telegramClientService.sendCallbackAnswer(
                textResourceService.getCallbackText(callbackTextCode, userLocale),
                callbackQueryId,
                true
        );
    }
}
