package ru.tbank.processor.service.filter.text.impl;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.FilterMode;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.entity.text.TextProcessingResult;
import ru.tbank.processor.service.filter.text.FilterCost;
import ru.tbank.processor.service.filter.text.TextEntityType;
import ru.tbank.processor.service.filter.text.TextFilter;

@Slf4j
@Component
public class LinksFilter extends TextFilter {

    public LinksFilter() {
        super(FilterCost.VERY_LOW);
    }

    @Override
    public TextProcessingResult process(@NonNull Message message, @NonNull TextModerationSettings moderationSettings) {
        var filterSettings = moderationSettings.getLinksFilterSettings();
        var checkResult = filterSettings.isEnabled() && message.hasEntities()
                && isContainsBlockedEntity(message, filterSettings, TextEntityType.URL);

        return checkResult? TextProcessingResult.LINK_FOUND: TextProcessingResult.OK;
    }

    @Override
    protected boolean isContainsBlockedEntity(@NonNull Message message, @NonNull TextFilterSettings filterSettings,
                                              TextEntityType entityType) {
        return message.getEntities().stream()
                .filter(entity -> entity.getType().equals(entityType.name().toLowerCase()))
                .anyMatch(entity -> {
                    var checkResult = filterSettings.getExclusions().stream()
                            .anyMatch(exclusion -> entity.getText().startsWith(exclusion));
                    return (filterSettings.getExclusionMode() == FilterMode.WHITE_LIST) != checkResult;
                });
    }
}
