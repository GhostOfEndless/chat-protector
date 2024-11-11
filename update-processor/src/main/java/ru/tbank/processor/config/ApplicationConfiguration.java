package ru.tbank.processor.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import ru.tbank.processor.service.personal.enums.UserState;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
public class ApplicationConfiguration {

    private static final String MESSAGES_PATH = "classpath:/messages/telegram/";
    private static final String GLOBAL_MESSAGES_PATH = MESSAGES_PATH + "global";

    @Bean
    public MessageSource messageSource() {
        var messageSource = new ReloadableResourceBundleMessageSource();
        String[] baseNames = Arrays.stream(UserState.values())
                .map(state -> MESSAGES_PATH + state.name().toLowerCase().replace('_', '-'))
                .toArray(String[]::new);

        messageSource.setBasenames(baseNames);
        messageSource.addBasenames(GLOBAL_MESSAGES_PATH);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());

        return messageSource;
    }
}
