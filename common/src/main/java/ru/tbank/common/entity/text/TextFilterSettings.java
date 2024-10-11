package ru.tbank.common.entity.text;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import ru.tbank.common.entity.FilterMode;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TextFilterSettings {

    private boolean enabled;
    @NotNull
    @Builder.Default
    private List<String> exclusions = new ArrayList<>();
    @NotNull
    @Builder.Default
    private FilterMode exclusionMode = FilterMode.WHITE_LIST;
}
