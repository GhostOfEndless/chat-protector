package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.Duration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        return new RestTemplate(requestFactory);
    }

    @Bean
    public TelegramClient telegramClient(@Value("${telegram.bot.token}") String botToken) {
        return new OkHttpTelegramClient(botToken);
    }

    @Bean
    public String botToken(@Value("${telegram.bot.token}") String botToken) {
        return botToken;
    }

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
