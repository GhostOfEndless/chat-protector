package ru.tbank.processor.service.personal.handlers.impl;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
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
import ru.tbank.processor.service.personal.UserRole;
import ru.tbank.processor.service.personal.UserState;
import ru.tbank.processor.service.personal.handlers.PersonalUpdateHandler;

import java.util.List;

@NullMarked
@Slf4j
@Component
public class StartStateHandler extends PersonalUpdateHandler {

    public StartStateHandler(
            AppUserService appUserService,
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService
    ) {
        super(appUserService, personalChatService, telegramClientService, textResourceService, UserState.START);
    }

    @Override
    protected void processUpdate(@NonNull Update update, @NonNull AppUserRecord userRecord) {
        UserRole userRole = UserRole.valueOf(userRecord.getRole());
        if (update.getMessage().hasText() && update.getMessage().getText().startsWith("/start")) {
            processStartCommand(userRole, userRecord);
        } else if (update.hasCallbackQuery()) {
            var pressedButton = TextSourceCode.valueOf(update.getCallbackQuery().getData());
            processCallbackButton(userRole, userRecord, pressedButton);
        }
    }

    private void processStartCommand(UserRole userRole, AppUserRecord userRecord) {
        switch (userRole) {
            // TODO: тут нужно добавить сообщение для USER
            case ADMIN -> sendMessage(
                    TextSourceCode.START_MESSAGE_ADMIN,
                    List.of(
                            TextSourceCode.START_BUTTON_CHATS,
                            TextSourceCode.START_BUTTON_ACCOUNT
                    ),
                    userRecord
            );
            case OWNER -> sendMessage(
                    TextSourceCode.START_MESSAGE_OWNER,
                    List.of(
                            TextSourceCode.START_BUTTON_ADMINS,
                            TextSourceCode.START_BUTTON_CHATS,
                            TextSourceCode.START_BUTTON_ACCOUNT
                    ),
                    userRecord
            );
        }
    }

    private void processCallbackButton(UserRole userRole, AppUserRecord userRecord, TextSourceCode pressedButton) {
        switch (pressedButton) {
            case START_BUTTON_CHATS -> {
                if (UserRole.ADMIN.isEqualOrLowerThan(userRole)) {
                    log.debug("User {} clicked button 'chats'", userRecord);
                } else {
                    log.warn("Permission denied for command 'chats' for user: {}", userRecord);
                }
            }
            case START_BUTTON_ADMINS -> {
                if (UserRole.OWNER.isEqualOrLowerThan(userRole)) {
                    log.debug("User {} clicked button 'admins'", userRecord);
                } else {
                    log.warn("Permission denied for command 'admins' for user: {}", userRecord);
                }
            }
            case START_BUTTON_ACCOUNT -> {
                if (UserRole.ADMIN.isEqualOrLowerThan(userRole)) {
                    log.debug("User {} clicked button 'account'", userRecord);
                } else {
                    log.warn("Permission denied for command 'account' for user: {}", userRecord);
                }
            }
        }
    }

    private void sendMessage(
            TextSourceCode messageText,
            List<TextSourceCode> buttonsText,
            AppUserRecord userRecord
    ) {
        var keyboardMarkup = buildKeyboard(buttonsText, userRecord.getLocale());

        try {
            Message sentMessage = telegramClientService.sendMessage(
                    userRecord.getId(),
                    textResourceService.getTextSource(
                            messageText,
                            null,
                            userRecord.getLocale()),
                    keyboardMarkup);

            personalChatService.save(userRecord.getId(), UserState.START.name(), sentMessage.getMessageId());
        } catch (TelegramApiException e) {
             log.error("Telegram API Error: {}", e.getMessage());
        }
    }

    private InlineKeyboardMarkup buildKeyboard(List<TextSourceCode> buttonsText, String userLocale) {
        var listOfRows = buttonsText.stream()
                .map(textCode -> new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(textResourceService.getTextSource(textCode, userLocale))
                                .callbackData(textCode.name())
                                .build()))
                .toList();

        return new InlineKeyboardMarkup(listOfRows);
    }
}
