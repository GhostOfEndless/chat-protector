package ru.tbank.receiver.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.tbank.common.telegram.MessageEntity;
import ru.tbank.common.telegram.enums.MessageEntityType;
import ru.tbank.receiver.exception.UnknownMessageEntityTypeException;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MessageEntityMapper {

    List<MessageEntity> toMessageEntityList(
            List<org.telegram.telegrambots.meta.api.objects.MessageEntity> messageEntities
    );

    @Mapping(target = "type", qualifiedByName = "parseEntityType", source = "type")
    MessageEntity toMessageEntity(org.telegram.telegrambots.meta.api.objects.MessageEntity messageEntity);

    @Named("parseEntityType")
    default MessageEntityType parseEntityType(String type) {
        if (!MessageEntityType.isEntityType(type)) {
            throw new UnknownMessageEntityTypeException("Message entity with type '%s' is unknown".formatted(type));
        }
        return MessageEntityType.getByType(type);
    }
}
