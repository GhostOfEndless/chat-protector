package ru.tbank.processor.service.personal.handlers;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.processor.excpetion.ChatModerationSettingsNotFoundException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.moderation.SpamProtectionSettingsService;
import ru.tbank.processor.service.persistence.GroupChatService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.CallbackAnswerSender;
import ru.tbank.processor.service.personal.MessageSender;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackAnswerPayload;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessageArgument;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

@Slf4j
@NullMarked
@Component
public class SpamProtectionStateHandler extends PersonalUpdateHandler {

    private final SpamProtectionSettingsService spamProtectionSettingsService;
    private final GroupChatService groupChatService;

    public SpamProtectionStateHandler(
            SpamProtectionSettingsService spamProtectionSettingsService,
            PersonalChatService personalChatService,
            CallbackAnswerSender callbackSender,
            GroupChatService groupChatService,
            MessageSender messageSender
    ) {
        super(personalChatService, callbackSender, messageSender, UserState.SPAM_PROTECTION);
        this.spamProtectionSettingsService = spamProtectionSettingsService;
        this.groupChatService = groupChatService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        long chatId = (Long) args[0];

        var spamSettings = spamProtectionSettingsService.getSettings(chatId);
        var buttonText = spamSettings.isEnabled()
                ? ButtonTextCode.SPAM_PROTECTION_DISABLE
                : ButtonTextCode.SPAM_PROTECTION_ENABLE;
        return groupChatService.findById(chatId)
                .map(chatRecord -> MessagePayload.create(
                        MessageTextCode.SPAM_PROTECTION_MESSAGE,
                        List.of(
                                MessageArgument.createTextArgument(String.valueOf(spamSettings.getCoolDownPeriod()))
                        ),
                        List.of(
                                CallbackButtonPayload.create(buttonText, chatId),
                                CallbackButtonPayload.create(ButtonTextCode.BACK, chatId)
                        )
                ))
                .orElseGet(chatNotFoundMessage);
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer callbackMessageId = callbackData.messageId();
        Long chatId = callbackData.getChatId();
        var pressedButton = callbackData.pressedButton();

        if (chatId == 0) {
            callbackSender.showChatUnavailableCallback(callbackData.callbackId(), userRecord.getLocale());
            return ProcessingResult.create(UserState.CHATS, callbackMessageId);
        }
        if (pressedButton.isBackButton()) {
            return ProcessingResult.create(UserState.CHAT, callbackMessageId, chatId);
        }
        if (!pressedButton.isSpamProtectionControlButton()) {
            return ProcessingResult.create(UserState.START, callbackMessageId);
        }

        return processSpamProtectionStateChanged(
                userRecord,
                pressedButton == ButtonTextCode.SPAM_PROTECTION_ENABLE,
                callbackData
        );
    }

    private ProcessingResult processSpamProtectionStateChanged(
            AppUserRecord userRecord,
            boolean newState,
            CallbackData callbackData
    ) {
        return checkPermissionAndProcess(
                UserRole.ADMIN,
                userRecord,
                () -> {
                    Integer messageId = callbackData.messageId();
                    Long chatId = callbackData.getChatId();
                    String callbackId = callbackData.callbackId();
                    String userLocale = userRecord.getLocale();
                    try {
                        log.debug("Spam protection new state is: {}", newState);
                        spamProtectionSettingsService.updateSettings(chatId, newState);
                        showSpamProtectionStateChangedCallback(userLocale, callbackId, newState);
                        goToState(userRecord, messageId, chatId);
                    } catch (ChatModerationSettingsNotFoundException ex) {
                        callbackSender.showChatUnavailableCallback(callbackId, userLocale);
                    }
                    return ProcessingResult.create(processedUserState, messageId, chatId);
                },
                callbackData
        );
    }

    private void showSpamProtectionStateChangedCallback(String userLocale, String callbackId, boolean newState) {
        var callbackText = newState
                ? CallbackTextCode.PROTECTION_ENABLE
                : CallbackTextCode.PROTECTION_DISABLE;
        callbackSender.showAnswerCallback(
                CallbackAnswerPayload.create(callbackText),
                userLocale,
                callbackId
        );
    }
}
