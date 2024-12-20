package ru.tbank.processor.service.group.filter.text.impl;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.entity.enums.TextProcessingResult;
import ru.tbank.processor.service.group.filter.text.FilterCost;
import ru.tbank.processor.service.group.filter.text.TextEntityType;
import ru.tbank.processor.service.group.filter.text.TextFilter;

@Slf4j
@NullMarked
@Component
public class LinksFilter extends TextFilter {

    public LinksFilter() {
        super(FilterCost.VERY_LOW);
    }

    @Override
    public TextProcessingResult process(Message message, TextModerationSettings moderationSettings) {
        var filterSettings = moderationSettings.getLinksFilterSettings();
        boolean checkResult = filterSettings.isEnabled()
                && message.hasEntities()
                && isContainsBlockedEntity(message, filterSettings, TextEntityType.URL);
        return checkResult
                ? TextProcessingResult.LINK_FOUND
                : TextProcessingResult.OK;
    }

    @Override
    protected boolean isContainsBlockedEntity(Message message, TextFilterSettings filterSettings,
                                              TextEntityType entityType) {
        return message.getEntities().stream()
                .filter(entity -> entityType.isTypeOf(entity.getType()))
                .anyMatch(entity -> {
                    String entityLink = entity.getText().replaceFirst("^https?://", "");
                    boolean contains = filterSettings.getExclusions().stream().anyMatch(entityLink::startsWith);
                    return calcCheckResult(filterSettings.getExclusionMode(), contains);
                });
    }
}
