package org.example.client.payload;

import java.util.List;

public record YandexGPTAnswer(
        List<Prediction> predictions
) {
}
