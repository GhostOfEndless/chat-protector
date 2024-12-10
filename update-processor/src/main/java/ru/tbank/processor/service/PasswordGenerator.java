package ru.tbank.processor.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import ru.tbank.processor.config.PasswordGenerationProperties;

@Component
@RequiredArgsConstructor
public class PasswordGenerator {

    private final PasswordGenerationProperties properties;

    public String generatePassword() {
        return RandomStringUtils.secureStrong()
                .nextAlphanumeric(properties.passwordLength());
    }
}
