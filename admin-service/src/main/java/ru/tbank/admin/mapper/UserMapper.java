package ru.tbank.admin.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.tbank.admin.controller.users.payload.UserResponse;
import ru.tbank.admin.pojo.ApplicationUser;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponse toUserResponse(ApplicationUser user);

    default Page<UserResponse> toResponsePage(@NonNull Page<ApplicationUser> applicationUsersPage) {
        List<UserResponse> responses = applicationUsersPage.getContent().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, applicationUsersPage.getPageable(), applicationUsersPage.getTotalElements());
    }
}
