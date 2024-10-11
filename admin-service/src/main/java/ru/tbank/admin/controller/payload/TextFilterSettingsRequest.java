package ru.tbank.admin.controller.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.tbank.common.entity.FilterMode;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TextFilterSettingsRequest {

    private Boolean enabled;
    private FilterMode exclusionMode;
    private List<String> exclusions;
}
