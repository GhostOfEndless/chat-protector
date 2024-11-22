package ru.tbank.processor.service.personal.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
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
import ru.tbank.processor.service.personal.enums.UserRole;
import ru.tbank.processor.service.personal.enums.UserState;
import ru.tbank.processor.service.personal.payload.CallbackAnswerPayload;
import ru.tbank.processor.service.personal.payload.CallbackArgument;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
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
    protected MessagePayload buildMessagePayloadForUser(UserRole userRole, Object[] args) {
        var languageButtons = Arrays.stream(Language.values())
                .map(language ->
                        CallbackButtonPayload.create(ButtonTextCode.getButtonForLanguage(language))
                )
                .collect(Collectors.toList());
        languageButtons.add(CallbackButtonPayload.create(ButtonTextCode.BUTTON_BACK));

        return MessagePayload.create(
                MessageTextCode.LANGUAGE_MESSAGE,
                // TODO: заменить на получение текущего языка пользователя
                List.of(
                        MessageArgument.createResourceArgument(LanguageTextCode.getFromLanguage(Language.RUSSIAN))
                ),
                languageButtons
        );
    }

    @Override
    protected ProcessingResult processCallbackButtonUpdate(CallbackQuery callbackQuery, AppUserRecord userRecord) {
        var callbackMessageId = callbackQuery.getMessage().getMessageId();
        var pressedButton = ButtonTextCode.valueOf(callbackQuery.getData());

        if (pressedButton.isBackButton()) {
            return ProcessingResult.create(UserState.START, callbackMessageId);
        }
        if (!pressedButton.isButtonLanguage()) {
            return ProcessingResult.create(processedUserState, callbackMessageId);
        }

        Language newLanguage = pressedButton.getLanguageFromButton();
        return processLanguageChanged(userRecord, newLanguage, callbackQuery);
    }

    private ProcessingResult processLanguageChanged(
            AppUserRecord userRecord,
            Language newLanguage,
            CallbackQuery callbackQuery
    ) {
        Integer messageId = callbackQuery.getMessage().getMessageId();
        appUserService.updateLocale(userRecord.getId(), newLanguage.getLanguageCode());
        userRecord.setLocale(newLanguage.getLanguageCode());
        showAnswerCallback(
                CallbackAnswerPayload.create(
                        CallbackTextCode.LANGUAGE_CHANGED,
                        List.of(
                                CallbackArgument.createResourceArgument(
                                        LanguageTextCode.getFromLanguage(newLanguage).getLanguageTextCode()
                                )
                        )),
                userRecord.getLocale(),
                callbackQuery.getId(),
                false
        );
        goToState(userRecord, messageId, new Object[]{});
        return ProcessingResult.create(processedUserState, messageId);
    }
}
