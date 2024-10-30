package ru.tbank.processor.service.filter.text.impl;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.FilterMode;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.entity.text.TextProcessingResult;
import ru.tbank.processor.service.filter.text.FilterCost;
import ru.tbank.processor.service.filter.text.TextEntityType;
import ru.tbank.processor.service.filter.text.TextFilter;

@NullMarked
@Component
public class BotCommandsFilter extends TextFilter {

    public BotCommandsFilter() {
        super(FilterCost.VERY_LOW);
    }

    @Override
    public TextProcessingResult process(Message message, TextModerationSettings moderationSettings) {
        var filterSettings = moderationSettings.getBotCommandsFilterSettings();
        var checkResult = filterSettings.isEnabled() && message.hasEntities()
                && isContainsBlockedEntity(message, filterSettings, TextEntityType.BOT_COMMAND);

        return checkResult ? TextProcessingResult.TAG_FOUND : TextProcessingResult.OK;
    }

    @Override
    protected boolean isContainsBlockedEntity(Message message, TextFilterSettings filterSettings,
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
