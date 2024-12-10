package ru.tbank.processor.service.personal;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.payload.CallbackAnswerPayload;

@Component
@RequiredArgsConstructor
public class CallbackAnswerSender {

    private final TelegramClientService telegramClientService;
    private final TextResourceService textResourceService;

    public final void showAnswerCallback(
            @NonNull CallbackAnswerPayload callbackAnswerPayload,
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

    public final void showAnswerCallback(
            CallbackAnswerPayload callbackAnswerPayload,
            String userLocale,
            String callbackQueryId
    ) {
        showAnswerCallback(callbackAnswerPayload, userLocale, callbackQueryId, false);
    }

    public final void showChatUnavailableCallback(String callbackId, String userLocale) {
        showAnswerCallback(
                CallbackAnswerPayload.create(
                        CallbackTextCode.CHAT_UNAVAILABLE
                ),
                userLocale,
                callbackId
        );
    }

    public void showMessageExpiredCallback(String userLocale, String callbackId) {
        showAnswerCallback(
                CallbackAnswerPayload.create(
                        CallbackTextCode.MESSAGE_EXPIRED
                ),
                userLocale,
                callbackId,
                true
        );
    }

    public void showPermissionDeniedCallback(String userLocale, String callbackId) {
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
