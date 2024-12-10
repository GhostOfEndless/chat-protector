package ru.tbank.admin.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.tbank.admin.controller.messages.DeletedMessageResponse;
import ru.tbank.admin.pojo.DeletedMessage;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DeletedMessageMapper {

    DeletedMessageResponse toDeletedMessageResponse(DeletedMessage deletedMessage);

    default Page<DeletedMessageResponse> toResponsePage(@NonNull Page<DeletedMessage> deletedMessagePage) {
        List<DeletedMessageResponse> responses = deletedMessagePage.getContent().stream()
                .map(this::toDeletedMessageResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, deletedMessagePage.getPageable(), deletedMessagePage.getTotalElements());
    }
}
