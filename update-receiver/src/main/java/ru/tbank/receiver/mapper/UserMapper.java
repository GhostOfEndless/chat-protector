package ru.tbank.receiver.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.tbank.common.telegram.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    User toTelegramUser(org.telegram.telegrambots.meta.api.objects.User user);
}
