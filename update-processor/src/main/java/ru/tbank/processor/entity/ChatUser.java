package ru.tbank.processor.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatUser {

    private Long userId;
    private Long chatId;
    private OffsetDateTime lastProcessedMessageDt;
}
