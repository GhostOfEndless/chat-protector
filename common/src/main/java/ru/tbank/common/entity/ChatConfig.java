package ru.tbank.common.entity;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ChatConfig implements Serializable {

    @NonNull
    private Long chatId;
    @NonNull
    private String chatName;
    private boolean isBlockLinks = false;
    @Builder.Default
    private List<String> allowedLinks = new ArrayList<>();
    private boolean isBlockTags = false;
    @Builder.Default
    private List<String> allowedTags = new ArrayList<>();
    private boolean isBlockMentions = false;
    @Builder.Default
    private List<String> allowedMentions = new ArrayList<>();
    private boolean isBlockPhoneNumbers = false;
    @Builder.Default
    private List<String> allowedPhoneNumbers = new ArrayList<>();
    private boolean isBlockEmails = false;
    @Builder.Default
    private List<String> allowedEmails = new ArrayList<>();
}
