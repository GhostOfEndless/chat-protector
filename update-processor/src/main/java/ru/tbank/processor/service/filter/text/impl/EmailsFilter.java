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
public class EmailsFilter extends TextFilter {

    public EmailsFilter() {
        super(FilterCost.VERY_LOW);
    }

    @Override
    public TextProcessingResult process(@NonNull Message message, @NonNull TextModerationSettings moderationSettings) {
        var filterSettings = moderationSettings.getEmailsFilterSettings();
        var checkResult = filterSettings.isEnabled() && message.hasEntities()
                && isContainsBlockedEntity(message, filterSettings, TextEntityType.EMAIL);

        return checkResult ? TextProcessingResult.EMAIL_FOUND : TextProcessingResult.OK;
    }
}
