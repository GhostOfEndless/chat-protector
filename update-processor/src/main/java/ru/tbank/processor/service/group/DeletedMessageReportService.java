package ru.tbank.processor.service.group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.tbank.common.entity.dto.DeletedTextMessageDTO;
import ru.tbank.common.entity.text.TextProcessingResult;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.DeletedTextMessageService;

@Service
@RequiredArgsConstructor
public class DeletedMessageReportService {

    private final DeletedTextMessageService deletedTextMessageService;
    private final AppUserService appUserService;

    public void saveReport(Message message, TextProcessingResult result) {
        var deletedTextMessage = DeletedTextMessageDTO.buildDto(message, result);
        var user = message.getFrom();
        appUserService.saveRegularUser(user.getId(), user.getFirstName(),
                user.getLastName(), user.getUserName());
        deletedTextMessageService.save(deletedTextMessage);
    }
}
