package org.example.client.payload;

public record Prediction(
        String label,
        double confidence
) {
}
