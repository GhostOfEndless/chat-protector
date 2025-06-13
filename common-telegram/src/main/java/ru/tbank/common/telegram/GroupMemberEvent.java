package ru.tbank.common.telegram;

import lombok.Builder;
import ru.tbank.common.telegram.enums.GroupMemberEventType;

import java.io.Serializable;

@Builder
public record GroupMemberEvent(
        GroupMemberEventType eventType,
        Chat chat,
        User user
) implements Serializable {
}
