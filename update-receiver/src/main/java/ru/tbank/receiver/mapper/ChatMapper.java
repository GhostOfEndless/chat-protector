package ru.tbank.receiver.mapper;

import org.jspecify.annotations.NullMarked;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.tbank.common.telegram.Chat;
import ru.tbank.common.telegram.enums.ChatType;
import ru.tbank.receiver.exception.UnknownChatTypeException;

@NullMarked
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatMapper {

    @Mapping(target = "type", qualifiedByName = "parseChatType", source = ".")
    Chat toGroupChat(org.telegram.telegrambots.meta.api.objects.chat.Chat chat);

    @Named("parseChatType")
    default ChatType parseChatType(org.telegram.telegrambots.meta.api.objects.chat.Chat chat) {
        if (chat.isGroupChat() || chat.isSuperGroupChat()) {
            return ChatType.GROUP;
        } else if (chat.isUserChat()) {
            return ChatType.PERSONAL;
        }
        throw new UnknownChatTypeException("Chat with type '%s' is unknown!".formatted(chat.getType()));
    }
}
