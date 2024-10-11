package ru.tbank.common.entity.text;

import lombok.*;
import org.jetbrains.annotations.NotNull;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TextModerationSettings {

    @NotNull
    @Builder.Default
    private TextFilterSettings linksFilterSettings = new TextFilterSettings();
    @NotNull
    @Builder.Default
    private TextFilterSettings tagsFilterSettings = new TextFilterSettings();
    @NotNull
    @Builder.Default
    private TextFilterSettings mentionsFilterSettings = new TextFilterSettings();
    @NotNull
    @Builder.Default
    private TextFilterSettings phoneNumbersFilterSettings = new TextFilterSettings();
    @NotNull
    @Builder.Default
    private TextFilterSettings emailsFilterSettings = new TextFilterSettings();
}
