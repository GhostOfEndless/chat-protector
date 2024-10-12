package ru.tbank.common.entity.text;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.tbank.common.entity.FilterMode;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TextFilterSettings {

    private Boolean enabled = false;
    @NotNull
    private List<String> exclusions = new ArrayList<>();
    @NotNull
    private FilterMode exclusionMode = FilterMode.WHITE_LIST;

}
