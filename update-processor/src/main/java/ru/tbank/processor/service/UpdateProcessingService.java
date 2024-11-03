package ru.tbank.processor.service;

import org.jspecify.annotations.NonNull;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProcessingService {

    void process(@NonNull Update update);
}
