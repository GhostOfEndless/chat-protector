package ru.tbank.processor.service.filter.text;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.FilterMode;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.entity.text.TextProcessingResult;

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
        var checkResult = textFilterSettings.isEnabled() && message.hasEntities()
                && isContainsBlockedEntity(message, textFilterSettings, entityType);

        return checkResult ? foundEntityResult : TextProcessingResult.OK;
    }

    protected boolean isContainsBlockedEntity(Message message, TextFilterSettings filterSettings,
                                              TextEntityType entityType) {
        return message.getEntities().stream()
                .filter(entity -> entity.getType().equals(entityType.name().toLowerCase()))
                .anyMatch(entity -> {
                    var checkResult = filterSettings.getExclusions().contains(entity.getText());
                    return (filterSettings.getExclusionMode() == FilterMode.WHITE_LIST) != checkResult;
                });
    }

    @Override
    public int compareTo(TextFilter anotherFilter) {
        return this.filterCost.compareTo(anotherFilter.filterCost);
    }
}
