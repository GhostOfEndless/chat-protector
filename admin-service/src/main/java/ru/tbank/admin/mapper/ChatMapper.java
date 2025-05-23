package ru.tbank.admin.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.tbank.admin.controller.chats.payload.ChatResponse;
import ru.tbank.admin.pojo.Chat;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatMapper {

    ChatResponse toChatResponse(Chat chat);

    List<ChatResponse> toChatResponseList(List<Chat> chats);
}
