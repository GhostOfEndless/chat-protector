package ru.tbank.common.entity.text;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class TextModerationSettings {

    @NotNull
    private TextFilterSettings linksFilterSettings = new TextFilterSettings();
    @NotNull
    private TextFilterSettings tagsFilterSettings = new TextFilterSettings();
    @NotNull
    private TextFilterSettings mentionsFilterSettings = new TextFilterSettings();
    @NotNull
    private TextFilterSettings phoneNumbersFilterSettings = new TextFilterSettings();
    @NotNull
    private TextFilterSettings emailsFilterSettings = new TextFilterSettings();
}
