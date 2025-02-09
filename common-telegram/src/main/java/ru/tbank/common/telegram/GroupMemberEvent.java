package ru.tbank.common.telegram;

import ru.tbank.common.telegram.enums.GroupMemberEventType;

import java.io.Serializable;

public record GroupMemberEvent(
        GroupMemberEventType eventType,
        Chat chat,
        User user
) implements Serializable {
}
