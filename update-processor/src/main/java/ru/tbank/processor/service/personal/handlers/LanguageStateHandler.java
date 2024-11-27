package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.TextResourceService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.PersonalChatService;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.enums.CallbackTextCode;
import ru.tbank.processor.service.personal.enums.Language;
import ru.tbank.processor.service.personal.enums.LanguageTextCode;
import ru.tbank.processor.service.personal.enums.MessageTextCode;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackAnswerPayload;
import ru.tbank.processor.service.personal.payload.CallbackArgument;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.service.personal.payload.MessageArgument;
import ru.tbank.processor.service.personal.payload.MessagePayload;
import ru.tbank.processor.service.personal.payload.ProcessingResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NullMarked
@Slf4j
@Component
public final class LanguageStateHandler extends PersonalUpdateHandler {

    private final AppUserService appUserService;

    public LanguageStateHandler(
            PersonalChatService personalChatService,
            TelegramClientService telegramClientService,
            TextResourceService textResourceService,
            AppUserService appUserService
    ) {
        super(personalChatService, telegramClientService, textResourceService, UserState.LANGUAGE);
        this.appUserService = appUserService;
    }

    @Override
    protected MessagePayload buildMessagePayloadForUser(AppUserRecord userRecord, Object[] args) {
        var languageButtons = Arrays.stream(Language.values())
                .map(language ->
                        CallbackButtonPayload.create(ButtonTextCode.getButtonForLanguage(language))
                )
                .collect(Collectors.toList());
        languageButtons.add(CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK));
        Language userLanguage = Language.fromCode(userRecord.getLocale());
        return MessagePayload.create(
                MessageTextCode.LANGUAGE_MESSAGE,
                List.of(
                        MessageArgument.createResourceArgument(LanguageTextCode.getFromLanguage(userLanguage))
                ),
                languageButtons
        );
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackData callbackData, AppUserRecord userRecord) {
        Integer messageId = callbackData.messageId();
        var pressedButton = callbackData.pressedButton();

        if (pressedButton.isBackButton()) {
            return ProcessingResult.create(UserState.START, messageId);
        }
        if (!pressedButton.isButtonLanguage()) {
            return ProcessingResult.create(processedUserState, messageId);
        }

        Language newLanguage = pressedButton.getLanguageFromButton();
        return processLanguageChanged(userRecord, newLanguage, callbackData);
    }

    private ProcessingResult processLanguageChanged(
            AppUserRecord userRecord,
            Language newLanguage,
            CallbackData callbackData
    ) {
        Integer messageId = callbackData.messageId();
        String callbackId = callbackData.callbackId();
        String userLocale = userRecord.getLocale();
        String newLocale = newLanguage.getLanguageCode();

        if (userLocale.equals(newLocale)) {
            showLanguageNotChangedCallback(userLocale, callbackId);
            return ProcessingResult.create(processedUserState, messageId);
        }

        appUserService.updateLocale(userRecord.getId(), newLocale);
        userRecord.setLocale(newLocale);
        showLanguageChangedCallback(newLanguage, userLocale, callbackId);
        goToState(userRecord, messageId);
        return ProcessingResult.create(processedUserState, messageId);
    }

    private void showLanguageChangedCallback(Language newLanguage, String userLocale, String callbackId) {
        showAnswerCallback(
                CallbackAnswerPayload.create(
                        CallbackTextCode.LANGUAGE_CHANGED,
                        List.of(
                                CallbackArgument.createResourceArgument(
                                        LanguageTextCode.getFromLanguage(newLanguage).getLanguageTextCode()
                                )
                        )),
                userLocale,
                callbackId
        );
    }

    private void showLanguageNotChangedCallback(String userLocale, String callbackId) {
        showAnswerCallback(
                CallbackAnswerPayload.create(
                        CallbackTextCode.LANGUAGE_NOT_CHANGED
                ),
                userLocale,
                callbackId
        );
    }
}
