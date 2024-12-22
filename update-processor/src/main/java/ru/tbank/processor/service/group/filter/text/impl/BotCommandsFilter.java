package ru.tbank.processor.service.group.filter.text.impl;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.common.entity.enums.TextProcessingResult;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.enums.MessageEntityType;
import ru.tbank.processor.service.group.filter.text.FilterCost;
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
                MessageEntityType.BOT_COMMAND, TextProcessingResult.BOT_COMMAND_FOUND);
    }
}
