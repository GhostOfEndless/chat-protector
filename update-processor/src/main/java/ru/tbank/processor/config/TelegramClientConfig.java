package ru.tbank.processor.config;

import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
@RequiredArgsConstructor
public class TelegramClientConfig {

    private final TelegramProperties telegramProperties;

    @Bean
    public TelegramClient telegramClient() {
        var tgUrl = TelegramUrl.builder()
                .schema(telegramProperties.schema())
                .host(telegramProperties.host())
                .port(telegramProperties.port())
                .build();
        return new OkHttpTelegramClient(new OkHttpClient.Builder().build(), telegramProperties.token(), tgUrl);
    }
}
