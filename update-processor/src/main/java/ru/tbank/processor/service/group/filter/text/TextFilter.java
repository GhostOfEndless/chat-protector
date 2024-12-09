package ru.tbank.processor.service.group.filter.text;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.enums.FilterMode;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.entity.enums.TextProcessingResult;

@NullMarked
@RequiredArgsConstructor
public abstract class TextFilter implements Comparable<TextFilter> {

    protected final FilterCost filterCost;

    public abstract TextProcessingResult process(Message message, TextModerationSettings moderationSettings);

    protected TextProcessingResult processBasicEntity(
            Message message,
            TextFilterSettings textFilterSettings,
            TextEntityType entityType,
            TextProcessingResult foundEntityResult
    ) {
        boolean checkResult = textFilterSettings.isEnabled()
                && message.hasEntities()
                && isContainsBlockedEntity(message, textFilterSettings, entityType);
        return checkResult
                ? foundEntityResult
                : TextProcessingResult.OK;
    }

    protected boolean isContainsBlockedEntity(Message message, TextFilterSettings filterSettings,
                                              TextEntityType entityType) {
        return message.getEntities().stream()
                .filter(entity -> entityType.isTypeOf(entity.getType()))
                .anyMatch(entity -> {
                    boolean contains = filterSettings.getExclusions().stream()
                            .anyMatch(exclusion -> entity.getText().startsWith(exclusion));
                    return calcCheckResult(filterSettings.getExclusionMode(), contains);
                });
    }

    protected boolean calcCheckResult(FilterMode mode, boolean contains) {
        return switch (mode) {
            case BLACK_LIST -> contains;
            case WHITE_LIST -> !contains;
        };
    }

    @Override
    public int compareTo(TextFilter anotherFilter) {
        return this.filterCost.compareTo(anotherFilter.filterCost);
    }
}
