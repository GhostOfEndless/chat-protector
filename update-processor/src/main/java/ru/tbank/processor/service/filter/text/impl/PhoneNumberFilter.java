package ru.tbank.processor.service.filter.text.impl;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.entity.text.TextProcessingResult;
import ru.tbank.processor.service.filter.text.FilterCost;
import ru.tbank.processor.service.filter.text.TextEntityType;
import ru.tbank.processor.service.filter.text.TextFilter;

@Component
public class PhoneNumberFilter extends TextFilter {

    public PhoneNumberFilter() {
        super(FilterCost.VERY_LOW);
    }

    @Override
    public TextProcessingResult process(@NonNull Message message, @NonNull TextModerationSettings moderationSettings) {
        var filterSettings = moderationSettings.getPhoneNumbersFilterSettings();
        var checkResult = filterSettings.isEnabled() && message.hasEntities()
                && isContainsBlockedEntity(message, filterSettings, TextEntityType.PHONE_NUMBER);

        return checkResult ? TextProcessingResult.PHONE_NUMBER_FOUND : TextProcessingResult.OK;
    }
}
