package ru.tbank.common.telegram;

import ru.tbank.common.telegram.enums.GroupMemberEventType;

public record GroupMemberEvent(
        GroupMemberEventType eventType,
        Chat chat
) {
}
