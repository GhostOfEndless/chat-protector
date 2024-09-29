package ru.tbank.common.entity;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ChatConfig {

    @NonNull
    private Long chatId;
    @NonNull
    private String chatName;
    private boolean isBlockLinks = false;
    private boolean isBlockTags = false;
}
