package ru.tbank.admin.exceptions;

import lombok.Getter;

@Getter
public class SpamProtectionExclusionValidationException extends ApplicationRuntimeException {

    private final Long exclusion;

    public SpamProtectionExclusionValidationException(Long exclusion) {
        super("spam-protection.exclusion.validation_error");
        this.exclusion = exclusion;
    }
}
