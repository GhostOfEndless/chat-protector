package ru.tbank.receiver.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.tbank.common.telegram.Message;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                ChatMapper.class,
                UserMapper.class,
                MessageEntityMapper.class
        },
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface MessageMapper {

    @Mapping(source = "from", target = "user")
    Message toMessage(org.telegram.telegrambots.meta.api.objects.message.Message message);
}
