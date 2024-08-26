package org.example.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.example.client.payload.Prediction;
import org.example.client.payload.YandexGPTAnswer;
import org.example.client.payload.YandexGPTPrompt;
import org.example.config.YandexGptConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class YandexCloudRestClientImpl implements YandexCloudRestClient {

    private final YandexGptConfiguration.KeyInfo yandexCloudKey;
    private final YandexGPTPrompt yandexGPTBasePrompt;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Duration tokenLiveDuration;
    private final String yandexCloudAuthUrl;
    private final String yandexCloudFolderID;
    private final String yandexCloudTextClassificationUrl;
    private ScheduledExecutorService tokenRenewalPool;
    private Semaphore semaphore;
    private String iamToken;

    @PostConstruct
    private void init() {
        semaphore = new Semaphore(1);
        tokenRenewalPool = Executors.newSingleThreadScheduledExecutor();

        tokenRenewalPool.scheduleAtFixedRate(() -> {
            try {
                semaphore.acquire();
                iamToken = getIamToken();
                semaphore.release();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 0, tokenLiveDuration.toSeconds(), TimeUnit.SECONDS);

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SneakyThrows
    @PreDestroy
    private void destroy() {
        tokenRenewalPool.shutdown();
        tokenRenewalPool.awaitTermination(1, TimeUnit.MINUTES);
    }

    private String getIamToken() {
        String jwt = getJwt();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var requestBody = new HashMap<>();
        requestBody.put("jwt", jwt);

        var request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    yandexCloudAuthUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readTree(response.getBody()).get("iamToken").asText();
            } else {
                log.error("Error while getting token");
            }

            return null;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }

        return null;
    }

    private String getJwt() {
        try {
            String privateKeyString = yandexCloudKey.private_key();
            String serviceAccountId = yandexCloudKey.service_account_id();
            String keyId = yandexCloudKey.id();

            PemObject privateKeyPem;
            try (PemReader reader = new PemReader(new StringReader(privateKeyString))) {
                privateKeyPem = reader.readPemObject();
            }

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyPem.getContent()));

            Instant now = Instant.now();

            // Формирование JWT.
            return Jwts.builder()
                    .header().add("kid", keyId)
                    .and()
                    .issuer(serviceAccountId)
                    .audience().add("https://iam.api.cloud.yandex.net/iam/v1/tokens")
                    .and()
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plusSeconds(tokenLiveDuration.toSeconds())))
                    .signWith(privateKey, Jwts.SIG.PS256)
                    .compact();

        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error(e.getMessage());
        }

        return null;
    }


    @Override
    public String classifyText(String text) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + iamToken);
        headers.add("x-folder-id", yandexCloudFolderID);

        var requestBody = buildPrompt(text);
        var request = new HttpEntity<>(requestBody, headers);

        try {
            semaphore.acquire();
            ResponseEntity<YandexGPTAnswer> response = restTemplate.postForEntity(
                    yandexCloudTextClassificationUrl, request, YandexGPTAnswer.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                List<Prediction> predictions = Objects.requireNonNull(response.getBody()).predictions();
                predictions.sort(Comparator.comparingDouble(Prediction::confidence).reversed());
                return predictions.getFirst().label();
            } else {
                log.error("Error while getting text classification");
            }

            return null;
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        } finally {
            semaphore.release();
        }

        return null;
    }

    private YandexGPTPrompt buildPrompt(String text) {
        return YandexGPTPrompt.builder()
                .text(text)
                .taskDescription(yandexGPTBasePrompt.taskDescription())
                .labels(yandexGPTBasePrompt.labels())
                .modelUri(yandexGPTBasePrompt.modelUri())
                .build();
    }
}
