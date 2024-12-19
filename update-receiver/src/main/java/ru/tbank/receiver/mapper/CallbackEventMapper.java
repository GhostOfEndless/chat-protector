package ru.tbank.receiver.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import ru.tbank.common.telegram.CallbackEvent;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = UserMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface CallbackEventMapper {

    @Mapping(source = "message", target = "messageId", qualifiedByName = "parseMessageId")
    @Mapping(source = "from", target = "user")
    CallbackEvent toCallbackEvent(CallbackQuery callbackQuery);

    @Named("parseMessageId")
    default Integer parseMessageId(@NonNull MaybeInaccessibleMessage maybeInaccessibleMessage) {
        return maybeInaccessibleMessage.getMessageId();
    }
}
