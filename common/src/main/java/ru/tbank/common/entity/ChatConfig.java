package ru.tbank.common.entity;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ChatConfig {

    private Long chatId;
    private boolean isBlockLinks = false;
    private boolean isBlockTags = false;
}
