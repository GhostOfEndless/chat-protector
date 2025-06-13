package ru.tbank.processor.entity;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatUser {

    private Long userId;
    private Long chatId;
    private OffsetDateTime lastProcessedMessageDt;
}
