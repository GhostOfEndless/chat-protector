package ru.tbank.processor.service;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.processor.utils.UpdateType;

public interface UpdateProcessingService {

    void process(UpdateType updateType, Update update);
}
