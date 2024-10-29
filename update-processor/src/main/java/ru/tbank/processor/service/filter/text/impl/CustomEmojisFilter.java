package ru.tbank.processor.service.filter.text.impl;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.stickers.GetCustomEmojiStickers;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.tbank.common.entity.FilterMode;
import ru.tbank.common.entity.text.TextFilterSettings;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.entity.text.TextProcessingResult;
import ru.tbank.processor.service.filter.text.FilterCost;
import ru.tbank.processor.service.filter.text.TextEntityType;
import ru.tbank.processor.service.filter.text.TextFilter;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class CustomEmojisFilter extends TextFilter {

    private final TelegramClient telegramClient;

    public CustomEmojisFilter(TelegramClient telegramClient) {
        super(FilterCost.MEDIUM);
        this.telegramClient = telegramClient;
    }

    @Override
    public TextProcessingResult process(@NonNull Message message, @NonNull TextModerationSettings moderationSettings) {
        var filterSettings = moderationSettings.getCustomEmojisFilterSettings();
        var checkResult = filterSettings.isEnabled() && message.hasEntities()
                && isContainsBlockedEntity(message, filterSettings, TextEntityType.CUSTOM_EMOJI);

        return checkResult ? TextProcessingResult.TAG_FOUND : TextProcessingResult.OK;
    }

    @Override
    protected boolean isContainsBlockedEntity(@NonNull Message message, @NonNull TextFilterSettings filterSettings,
                                              TextEntityType entityType) {
        return message.getEntities().stream()
                .filter(entity -> entity.getType().equals(entityType.name().toLowerCase()))
                .anyMatch(entity -> {
                    var stickerSet = getEmojiPack(entity.getCustomEmojiId());
                    var checkResult = filterSettings.getExclusions().contains(stickerSet.getFirst().getSetName());
                    return (filterSettings.getExclusionMode() == FilterMode.WHITE_LIST) != checkResult;
                });
    }

    private List<Sticker> getEmojiPack(String customEmojiId) {
        try {
            var getCustomEmojiStickers = new GetCustomEmojiStickers(List.of(customEmojiId));
            return telegramClient.execute(getCustomEmojiStickers);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }
}