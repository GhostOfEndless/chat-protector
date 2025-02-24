package ru.tbank.receiver.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tbank.common.telegram.CallbackEvent;
import ru.tbank.common.telegram.GroupMemberEvent;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.enums.UpdateType;
import ru.tbank.receiver.exception.UnknownUpdateTypeException;
import ru.tbank.receiver.mapper.CallbackEventMapper;
import ru.tbank.receiver.mapper.GroupMemberEventMapper;
import ru.tbank.receiver.mapper.MessageMapper;

@NullMarked
@Service
@RequiredArgsConstructor
public class UpdateParserService {

    private final MessageMapper messageMapper;
    private final CallbackEventMapper callbackEventMapper;
    private final GroupMemberEventMapper groupMemberEventMapper;

    public UpdateType parseUpdateType(Update update) {
        if (update.hasMessage()) {
            return parseMessageUpdateType(update);
        } else if (update.hasMyChatMember()
                && update.getMyChatMember().getChat().isGroupChat()
                && update.getMyChatMember().getChat().isSuperGroupChat()
        ) {
            return UpdateType.GROUP_MEMBER_EVENT;
        } else if (update.hasCallbackQuery()) {
            return UpdateType.CALLBACK_EVENT;
        }
        throw new UnknownUpdateTypeException(update);
    }

    public Message parseMessageFromUpdate(Update update) {
        return messageMapper.toMessage(update.getMessage());
    }

    public GroupMemberEvent parseGroupMemberEventFromUpdate(Update update) {
        return groupMemberEventMapper.toGroupMemberEvent(update.getMyChatMember());
    }

    public CallbackEvent parseCallbackEventFromUpdate(Update update) {
        return callbackEventMapper.toCallbackEvent(update.getCallbackQuery());
    }

    private UpdateType parseMessageUpdateType(Update update) {
        var chat = update.getMessage().getChat();
        if (chat.isGroupChat() || chat.isSuperGroupChat()) {
            return UpdateType.GROUP_MESSAGE;
        } else if (chat.isUserChat()) {
            return UpdateType.PERSONAL_MESSAGE;
        }
        throw new UnknownUpdateTypeException(update);
    }
}
