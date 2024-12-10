package ru.tbank.admin.pojo;

import lombok.Data;

import java.time.LocalDateTime;

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
