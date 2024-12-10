package ru.tbank.processor.service.personal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.MessagePayload;

import java.util.List;

@Slf4j
@NullMarked
@Component
@RequiredArgsConstructor
public class MessageSender {

    private final PersonalChatService personalChatService;
    private final TextResourceService textResourceService;
    private final TelegramClientService telegramClientService;

    public void updateUserMessage(AppUserRecord userRecord, Integer messageId, MessagePayload payload, String state) {
        var keyboardMarkup = buildKeyboard(payload.buttons(), userRecord.getLocale());
        String messageText = buildMessageText(userRecord.getLocale(), payload);
        Long userId = userRecord.getId();
        try {
            if (messageId == 0) {
                var sentMessage = telegramClientService.sendMessage(userId, messageText, keyboardMarkup);
                personalChatService.save(userId, state, sentMessage.getMessageId());
                return;
            }
            telegramClientService.editMessage(userId, messageId, messageText, keyboardMarkup);
            personalChatService.save(userId, state, messageId);
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

    private InlineKeyboardMarkup buildKeyboard(List<CallbackButtonPayload> callbackButtons, String userLocale) {
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
}
