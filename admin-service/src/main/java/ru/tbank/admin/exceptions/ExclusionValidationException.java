package ru.tbank.admin.exceptions;

import lombok.Getter;

@Getter
public class ExclusionValidationException extends ApplicationRuntimeException {

    private final String exclusion;

    public ExclusionValidationException(String exclusion) {
        super("text_filter.exclusion.validation_error");
        this.exclusion = exclusion;
    }
}
