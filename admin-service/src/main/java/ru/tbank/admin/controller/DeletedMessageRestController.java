package ru.tbank.admin.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.admin.controller.payload.DeletedMessageResponse;
import ru.tbank.admin.mapper.DeletedMessageMapper;
import ru.tbank.admin.service.persistence.DeletedMessageService;
import ru.tbank.admin.service.persistence.GroupChatService;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Удалённые сообщения", description = "API удалённых сообщений")
public class DeletedMessageRestController {

    private final DeletedMessageService deletedMessageService;
    private final GroupChatService groupChatService;
    private final DeletedMessageMapper deletedMessageMapper;

    @GetMapping("/api/v1/admin/chats/{chatId}/deleted-messages")
    public PagedModel<DeletedMessageResponse> getDeletedMessages(
            @PathVariable("chatId")
            @Parameter(description = "ID Telegram чата", example = "-123456789", required = true)
            Long id,
            @RequestParam int page,
            @RequestParam int size
    ) {
        groupChatService.getGroupChatById(id);
        var deletedMessages = deletedMessageService.getDeletedMessages(id, PageRequest.of(page - 1, size));
        var responses = deletedMessageMapper.toResponsePage(deletedMessages);
        return new PagedModel<>(responses);
    }
}
