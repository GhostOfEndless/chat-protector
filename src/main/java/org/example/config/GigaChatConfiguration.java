package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("gigachat")
@Configuration
public class GigaChatConfiguration {

    @Bean
    public String systemPrompt(@Value("${gigachat.system-prompt}") String systemPrompt) {
        return systemPrompt;
    }

    @Bean
    public String apiAuthData(@Value("${gigachat.auth-data}") String authData) {
        return authData;
    }

    @Bean
    public String apiScope(@Value("${gigachat.scope}") String apiScope) {
        return apiScope;
    }

    @Bean
    public String authUri(@Value("${gigachat.auth-uri}") String authUri) {
        return authUri;
    }

    @Bean
    public String chatUri(@Value("${gigachat.chat-uri}") String chatUri) {
        return chatUri;
    }
}
