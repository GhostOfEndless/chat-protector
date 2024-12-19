package ru.tbank.common.telegram;

import org.jspecify.annotations.NonNull;
import ru.tbank.common.telegram.enums.UpdateType;

public record TelegramUpdate(
        UpdateType updateType,
        Message message,
        CallbackEvent callbackEvent,
        GroupMemberEvent groupMemberEvent
) {
    public static @NonNull TelegramUpdate createMessageUpdate(Message message) {
        return new TelegramUpdate(UpdateType.MESSAGE, message, null, null);
    }

    public static @NonNull TelegramUpdate createCallbackEventUpdate(CallbackEvent callbackEvent) {
        return new TelegramUpdate(UpdateType.CALLBACK_EVENT, null, callbackEvent, null);
    }

    public static @NonNull TelegramUpdate createGroupMemberEventUpdate(GroupMemberEvent groupMemberEvent) {
        return new TelegramUpdate(UpdateType.GROUP_MEMBER_EVENT, null, null, groupMemberEvent);
    }
}
