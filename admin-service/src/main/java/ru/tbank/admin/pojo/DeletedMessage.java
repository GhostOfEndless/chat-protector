package ru.tbank.admin.pojo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DeletedMessage {

    private Long id;
    private Long chatId;
    private Integer messageId;
    private String messageText;
    private Long userId;
    private LocalDateTime deletionTime;
    private String reason;
}
