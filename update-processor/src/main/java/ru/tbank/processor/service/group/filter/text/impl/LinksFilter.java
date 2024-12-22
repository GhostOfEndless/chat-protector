package ru.tbank.processor.service.group.filter.text.impl;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.common.entity.enums.TextProcessingResult;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.enums.MessageEntityType;
import ru.tbank.processor.service.group.filter.text.FilterCost;
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
                && isContainsBlockedEntity(message, filterSettings, MessageEntityType.URL);
        return checkResult
                ? TextProcessingResult.LINK_FOUND
                : TextProcessingResult.OK;
    }

    @Override
    protected boolean isContainsBlockedEntity(Message message, TextFilterSettings filterSettings,
                                              MessageEntityType entityType) {
        return message.entities().stream()
                .filter(entity -> entity.type() == entityType)
                .anyMatch(entity -> {
                    String entityLink = entity.text().replaceFirst("^https?://", "");
                    boolean contains = filterSettings.getExclusions().stream().anyMatch(entityLink::startsWith);
                    return calcCheckResult(filterSettings.getExclusionMode(), contains);
                });
    }
}
