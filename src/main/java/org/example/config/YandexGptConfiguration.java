package org.example.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.client.payload.YandexGPTPrompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

@Slf4j
@Profile("yandex-gpt")
@Configuration
public class YandexGptConfiguration {

    @Bean
    public KeyInfo yandexCloudKey(@Value("${yandex-cloud.authorized-key-filename}") String filename) throws Exception {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filename)));
            return new ObjectMapper().readValue(content, KeyInfo.class);
        } catch (IOException e) {
            throw new Exception("Error in " + filename + " file!");
        }
    }

    @Bean
    public YandexGPTPrompt yandexGPTPrompt(@Value("${yandex-cloud.folder-id}") String folderId) throws Exception {
        try {
            var content = new String(Files.readAllBytes(Paths.get(
                    "C:\\Users\\Aleksey\\IdeaProjects\\chat-protector\\src\\main\\resources\\yandex-cloud-configs\\prompt.json")));
            var basePrompt = new ObjectMapper().readValue(content, YandexGPTPrompt.class);

            return YandexGPTPrompt.builder()
                    .modelUri(basePrompt.modelUri().replace("<folder-id>", folderId))
                    .taskDescription(basePrompt.taskDescription())
                    .labels(basePrompt.labels())
                    .build();
        } catch (IOException e) {
            throw new Exception("Error in prompt.json file!");
        }
    }

    @Bean
    public Duration tokenLiveDuration() {
        return Duration.ofHours(1);
    }

    @Bean
    public String yandexCloudAuthUrl(@Value("${yandex-cloud.auth-url}") String authUri) {
        return authUri;
    }

    @Bean
    public String yandexCloudFolderID(@Value("${yandex-cloud.folder-id}") String folderId) {
        return folderId;
    }

    @Bean
    public String yandexCloudTextClassificationUrl(@Value("${yandex-cloud.text-classification-url}") String url) {
        return url;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KeyInfo(
            String id,
            String service_account_id,
            String private_key
    ) {
    }
}
