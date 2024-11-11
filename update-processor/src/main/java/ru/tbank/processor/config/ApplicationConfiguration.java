package ru.tbank.processor.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public MessageSource messageSource() {
        var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
                "classpath:/messages/telegram/chat",
                "classpath:/messages/telegram/chats",
                "classpath:/messages/telegram/global",
                "classpath:/messages/telegram/filters",
                "classpath:/messages/telegram/start",
                "classpath:/messages/telegram/text-filters"
        );
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }
}
