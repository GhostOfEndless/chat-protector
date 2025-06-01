package ru.tbank.admin.exceptions;

import lombok.Getter;

@Getter
public class TextFilterExclusionValidationException extends ApplicationRuntimeException {

    private final String exclusion;

    public TextFilterExclusionValidationException(String exclusion) {
        super("text_filter.exclusion.validation_error");
        this.exclusion = exclusion;
    }
}
