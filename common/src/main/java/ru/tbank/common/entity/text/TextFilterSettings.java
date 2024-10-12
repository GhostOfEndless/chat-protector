package ru.tbank.common.entity.text;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.tbank.common.entity.FilterMode;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class TextFilterSettings {

    private Boolean enabled = false;
    @NotNull
    private List<String> exclusions = new ArrayList<>();
    @NotNull
    private FilterMode exclusionMode = FilterMode.WHITE_LIST;
}
