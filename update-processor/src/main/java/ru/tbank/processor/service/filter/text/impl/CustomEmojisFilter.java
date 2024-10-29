package ru.tbank.processor.service.filter.text.impl;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.FilterMode;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.entity.text.TextProcessingResult;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.filter.text.FilterCost;
import ru.tbank.processor.service.filter.text.TextEntityType;
import ru.tbank.processor.service.filter.text.TextFilter;

@Slf4j
@NullMarked
@Component
public class CustomEmojisFilter extends TextFilter {

    private final TelegramClientService telegramClientService;

    public CustomEmojisFilter(TelegramClientService telegramClientService) {
        super(FilterCost.MEDIUM);
        this.telegramClientService = telegramClientService;
    }

    @Override
    public TextProcessingResult process(Message message, TextModerationSettings moderationSettings) {
        var filterSettings = moderationSettings.getCustomEmojisFilterSettings();
        var checkResult = filterSettings.isEnabled() && message.hasEntities()
                && isContainsBlockedEntity(message, filterSettings, TextEntityType.CUSTOM_EMOJI);

        return checkResult ? TextProcessingResult.TAG_FOUND : TextProcessingResult.OK;
    }

    @Override
    protected boolean isContainsBlockedEntity(Message message, TextFilterSettings filterSettings,
                                              TextEntityType entityType) {
        return message.getEntities().stream()
                .filter(entity -> entity.getType().equals(entityType.name().toLowerCase()))
                .anyMatch(entity -> {
                    var stickerSet = telegramClientService.getEmojiPack(entity.getCustomEmojiId());
                    var checkResult = filterSettings.getExclusions().contains(stickerSet.getFirst().getSetName());
                    return (filterSettings.getExclusionMode() == FilterMode.WHITE_LIST) != checkResult;
                });
    }
}
