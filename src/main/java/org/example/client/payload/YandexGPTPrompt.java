package org.example.client.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record YandexGPTPrompt(
        String modelUri,
        String text,
        @JsonProperty("task_description")
        String taskDescription,
        List<String> labels
) {
}
