package org.example.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GigaChatRestClientImpl implements GigaChatRestClient {

    private final RestTemplate restTemplate;
    private final String apiAuthData;
    private final String apiScope;
    private final String authUri;
    private final String chatUri;
    private final String systemPrompt;
    private final ObjectMapper objectMapper;
    private ScheduledExecutorService tokenRenewalPool;
    private Semaphore semaphore;
    private String bearerToken;

    @PostConstruct
    private void init() {
        semaphore = new Semaphore(1);
        tokenRenewalPool = Executors.newSingleThreadScheduledExecutor();

        tokenRenewalPool.scheduleAtFixedRate(() -> {
            try {
                bearerToken = getBearerToken();
                log.info("Token is: {}", bearerToken);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 0, 10, TimeUnit.MINUTES);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SneakyThrows
    @PreDestroy
    private void onDestroy() {
        tokenRenewalPool.shutdown();
        tokenRenewalPool.awaitTermination(1, TimeUnit.MINUTES);
    }

    private String getBearerToken() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "Basic " + apiAuthData);
        headers.add("RqUID", UUID.randomUUID().toString());

        var requestBody = new LinkedMultiValueMap<>();
        requestBody.add("scope", apiScope);

        var request = new HttpEntity<>(requestBody, headers);

        try {
            semaphore.acquire();
            ResponseEntity<String> response = restTemplate.postForEntity(authUri, request, String.class);
            semaphore.release();

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Success!");
                return objectMapper.readTree(response.getBody()).get("access_token").asText();
            } else {
                log.error("Error while getting token");
            }

            return null;
        } catch (JsonProcessingException | InterruptedException e) {
            log.error(e.getMessage());
        }

        return null;
    }

    @Override
    public String getAssistAnswer(String requestText) {
        if (bearerToken == null) {
            return "invalid-token";
        }

        var requestBody = buildRequestBody(requestText);
        var headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + bearerToken);
        var request = new HttpEntity<>(requestBody, headers);
        try {
            log.info(request.toString());
            semaphore.acquire();
            ResponseEntity<String> response = this.restTemplate.postForEntity(chatUri, request, String.class);
            semaphore.release();

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode choicesNode = objectMapper.readTree(response.getBody()).get("choices");
                if (choicesNode.isArray() && !choicesNode.isEmpty()) {
                    JsonNode firstChoice = choicesNode.get(0);
                    JsonNode messageNode = firstChoice.get("message");
                    JsonNode finishReason = firstChoice.get("finish_reason");
                    if (messageNode != null) {
                        JsonNode contentNode = messageNode.get("content");
                        if (contentNode != null && finishReason != null) {
                            if (finishReason.asText().equals("stop")) {
                                return contentNode.asText();
                            } else if (finishReason.asText().equals("blacklist")) {
                                return "1";
                            }
                        }
                    }
                }
            }
        } catch (RestClientException | JsonProcessingException | InterruptedException e) {
            log.error(e.getMessage());
        }

        return "api-error";
    }

    private HashMap<Object, Object> buildRequestBody(String requestText) {
        var requestBody = new HashMap<>();
        requestBody.put("model", "GigaChat");
        requestBody.put("temperature", 0.01);
        requestBody.put("max_tokens", 5);
        var messages = new ArrayList<>();

        // add system prompt
        messages.add(new HashMap<>() {{
            put("role", "system");
            put("content", systemPrompt);
        }});

        // add user request
        messages.add(new HashMap<>() {{
            put("role", "user");
            put("content", requestText);
        }});

        requestBody.put("messages", messages);

        return requestBody;
    }
}
