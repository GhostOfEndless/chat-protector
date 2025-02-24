package ru.tbank.processor.config;

import java.nio.charset.StandardCharsets;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.tbank.processor.service.personal.enums.UserState;

@Configuration
public class ApplicationConfiguration {

    private static final String MESSAGES_PATH = "classpath:/messages/telegram/";
    private static final String GLOBAL_MESSAGES_PATH = MESSAGES_PATH + "global";

    @Bean
    public MessageSource messageSource() {
        var messageSource = new ReloadableResourceBundleMessageSource();
        String[] baseNames = UserState.getBASE_NAMES().stream()
                .map(baseName -> MESSAGES_PATH + baseName)
                .toArray(String[]::new);

        messageSource.setBasenames(baseNames);
        messageSource.addBasenames(GLOBAL_MESSAGES_PATH);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        return messageSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
