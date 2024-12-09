package ru.tbank.admin.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.tbank.admin.controller.payload.ChatResponse;
import ru.tbank.admin.entity.Chat;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatMapper {

    ChatResponse toChatResponse(Chat chat);

    List<ChatResponse> toChatResponseList(List<Chat> chats);
}
