package ru.tbank.common.entity.text;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TextModerationSettings {

    private TextFilterSettings linksFilterSettings = new TextFilterSettings();
    private TextFilterSettings tagsFilterSettings = new TextFilterSettings();
    private TextFilterSettings mentionsFilterSettings = new TextFilterSettings();
    private TextFilterSettings phoneNumbersFilterSettings = new TextFilterSettings();
    private TextFilterSettings emailsFilterSettings = new TextFilterSettings();
    private TextFilterSettings botCommandsFilterSettings = new TextFilterSettings();
    private TextFilterSettings customEmojisFilterSettings = new TextFilterSettings();
}
