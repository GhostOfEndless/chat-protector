package ru.tbank.common.entity.text;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString
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
