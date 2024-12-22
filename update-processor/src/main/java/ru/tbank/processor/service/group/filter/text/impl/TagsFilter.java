package ru.tbank.processor.service.group.filter.text.impl;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import ru.tbank.common.entity.enums.TextProcessingResult;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.enums.MessageEntityType;
import ru.tbank.processor.service.group.filter.text.FilterCost;
import ru.tbank.processor.service.group.filter.text.TextFilter;

@Slf4j
@NullMarked
@Component
public class TagsFilter extends TextFilter {

    public TagsFilter() {
        super(FilterCost.VERY_LOW);
    }

    @Override
    public TextProcessingResult process(Message message, TextModerationSettings moderationSettings) {
        return processBasicEntity(message, moderationSettings.getTagsFilterSettings(),
                MessageEntityType.HASHTAG, TextProcessingResult.TAG_FOUND);
    }
}
