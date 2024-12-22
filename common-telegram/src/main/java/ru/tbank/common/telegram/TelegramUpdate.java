package ru.tbank.common.telegram;

import org.jspecify.annotations.NonNull;
import ru.tbank.common.telegram.enums.UpdateType;

import java.io.Serializable;

public record TelegramUpdate(
        UpdateType updateType,
        Message message,
        CallbackEvent callbackEvent,
        GroupMemberEvent groupMemberEvent
) implements Serializable {
    public static @NonNull TelegramUpdate createPersonalMessageUpdate(Message message) {
        return new TelegramUpdate(UpdateType.PERSONAL_MESSAGE, message, null, null);
    }

    public static @NonNull TelegramUpdate createGroupMessageUpdate(Message message) {
        return new TelegramUpdate(UpdateType.GROUP_MESSAGE, message, null, null);
    }

    public static @NonNull TelegramUpdate createCallbackEventUpdate(CallbackEvent callbackEvent) {
        return new TelegramUpdate(UpdateType.CALLBACK_EVENT, null, callbackEvent, null);
    }

    public static @NonNull TelegramUpdate createGroupMemberEventUpdate(GroupMemberEvent groupMemberEvent) {
        return new TelegramUpdate(UpdateType.GROUP_MEMBER_EVENT, null, null, groupMemberEvent);
    }
}
