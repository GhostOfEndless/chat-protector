package ru.tbank.processor.service.group.filter.text.impl;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.entity.enums.TextProcessingResult;
import ru.tbank.processor.service.group.filter.text.FilterCost;
import ru.tbank.processor.service.group.filter.text.TextEntityType;
import ru.tbank.processor.service.group.filter.text.TextFilter;

@NullMarked
@Component
public class BotCommandsFilter extends TextFilter {

    public BotCommandsFilter() {
        super(FilterCost.VERY_LOW);
    }

    @Override
    public TextProcessingResult process(Message message, TextModerationSettings moderationSettings) {
        return processBasicEntity(message, moderationSettings.getBotCommandsFilterSettings(),
                TextEntityType.BOT_COMMAND, TextProcessingResult.BOT_COMMAND_FOUND);
    }
}
