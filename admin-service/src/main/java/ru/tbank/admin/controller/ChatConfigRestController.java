package ru.tbank.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.admin.service.ChatConfigService;
import ru.tbank.common.entity.ChatConfig;

@RestController
@RequestMapping("/api/v1/chats/{chatId}")
@RequiredArgsConstructor
public class ChatConfigRestController {

    private final ChatConfigService chatConfigService;

    @GetMapping
    public ChatConfig getChatConfig(@PathVariable("chatId") Long id) {
        return chatConfigService.getChatConfig(id);
    }
}
