package ru.tbank.processor.service.group.filter.text.impl;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.entity.enums.TextProcessingResult;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.group.filter.text.FilterCost;
import ru.tbank.processor.service.group.filter.text.TextEntityType;
import ru.tbank.processor.service.group.filter.text.TextFilter;

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
        boolean checkResult = filterSettings.isEnabled()
                && message.hasEntities()
                && isContainsBlockedEntity(message, filterSettings, TextEntityType.CUSTOM_EMOJI);
        return checkResult
                ? TextProcessingResult.CUSTOM_EMOJI_FOUND
                : TextProcessingResult.OK;
    }

    @Override
    protected boolean isContainsBlockedEntity(Message message, TextFilterSettings filterSettings,
                                              TextEntityType entityType) {
        return message.getEntities().stream()
                .filter(entity -> entityType.isTypeOf(entity.getType()))
                .anyMatch(entity -> {
                    var stickerSet = telegramClientService.getEmojiPack(entity.getCustomEmojiId());
                    boolean contains = filterSettings.getExclusions().contains(stickerSet.getFirst().getSetName());
                    return calcCheckResult(filterSettings.getExclusionMode(), contains);
                });
    }
}
