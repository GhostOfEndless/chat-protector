package ru.tbank.receiver.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberUpdated;
import ru.tbank.common.telegram.GroupMemberEvent;
import ru.tbank.common.telegram.enums.GroupMemberEventType;
import ru.tbank.receiver.exception.UnknownGroupMemberEventTypeException;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                ChatMapper.class,
                UserMapper.class
        },
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface GroupMemberEventMapper {

    String LEFT_ACTION = "left";
    String KICKED_ACTION = "kicked";

    @Mapping(source = "from", target = "user")
    @Mapping(source = ".", target = "eventType", qualifiedByName = "parseGroupMemberEventType")
    GroupMemberEvent toGroupMemberEvent(ChatMemberUpdated chatMemberUpdated);

    @Named("parseGroupMemberEventType")
    default GroupMemberEventType parseGroupMemberEventType(@NonNull ChatMemberUpdated chatMemberUpdated) {
        String oldStatus = chatMemberUpdated.getOldChatMember().getStatus();
        String newStatus = chatMemberUpdated.getNewChatMember().getStatus();
        if ((oldStatus.equals(LEFT_ACTION) || oldStatus.equals(KICKED_ACTION))
                && (newStatus.equals("administrator") || newStatus.equals("member"))) {
            return GroupMemberEventType.GROUP_BOT_ADDED;
        } else if (newStatus.equals(LEFT_ACTION) || newStatus.equals(KICKED_ACTION)) {
            return chatMemberUpdated.getFrom().getIsBot()
                    ? GroupMemberEventType.GROUP_BOT_LEFT
                    : GroupMemberEventType.GROUP_BOT_KICKED;
        }
        throw new UnknownGroupMemberEventTypeException(
                "Unknown group member event type for %s".formatted(chatMemberUpdated)
        );
    }
}
