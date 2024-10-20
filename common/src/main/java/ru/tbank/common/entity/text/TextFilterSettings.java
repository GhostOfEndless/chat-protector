package ru.tbank.common.entity.text;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tbank.common.entity.FilterMode;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TextFilterSettings {

    private boolean enabled;
    private List<String> exclusions = new ArrayList<>();
    private FilterMode exclusionMode = FilterMode.WHITE_LIST;

}
