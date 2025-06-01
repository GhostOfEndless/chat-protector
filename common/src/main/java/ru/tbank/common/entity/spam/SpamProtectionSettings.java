package ru.tbank.common.entity.spam;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class SpamProtectionSettings {

    private boolean enabled;
    private long coolDownPeriod;
    private Set<Long> exclusions = new HashSet<>();
}
