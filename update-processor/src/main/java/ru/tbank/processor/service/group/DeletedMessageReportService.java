package ru.tbank.processor.service.group;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import ru.tbank.common.entity.enums.TextProcessingResult;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.User;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.DeletedTextMessageService;

@Service
@RequiredArgsConstructor
public class DeletedMessageReportService {

    private static final String SPAM_REASON = "SPAM";

    private final DeletedTextMessageService deletedTextMessageService;
    private final AppUserService appUserService;

    public void saveReport(@NonNull Message message, @NonNull TextProcessingResult result) {
        User user = message.user();
        appUserService.save(message.user(), user.userName());
        deletedTextMessageService.save(message, result.name());
    }

    public void saveSpamReport(@NonNull Message message) {
        User user = message.user();
        appUserService.save(message.user(), user.userName());
        deletedTextMessageService.save(message, SPAM_REASON);
    }
}
