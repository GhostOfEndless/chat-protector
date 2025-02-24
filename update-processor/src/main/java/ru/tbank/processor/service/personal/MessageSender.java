package ru.tbank.processor.service.personal;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tbank.processor.excpetion.StateNotUpdatedException;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.MessagePayload;

@Slf4j
@NullMarked
@Component
@RequiredArgsConstructor
public class MessageSender {

    private final TextResourceService textResourceService;
    private final TelegramClientService telegramClientService;

    public Integer updateUserMessage(Long userId, String userLocale, Integer messageId, MessagePayload payload) {
        var keyboardMarkup = buildKeyboard(payload.buttons(), userLocale);
        String messageText = buildMessageText(userLocale, payload);
        try {
            if (messageId == 0) {
                return telegramClientService.sendMessage(userId, messageText, keyboardMarkup).getMessageId();
            }
            telegramClientService.editMessage(userId, messageId, messageText, keyboardMarkup);
            return messageId;
        } catch (TelegramApiException e) {
            log.error("Telegram API Error: {}", e.getMessage());
            throw new StateNotUpdatedException("User state not updated due to API exception");
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
