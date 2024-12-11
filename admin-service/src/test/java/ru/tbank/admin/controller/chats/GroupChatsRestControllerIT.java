package ru.tbank.admin.controller.chats;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import ru.tbank.admin.BaseIT;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GroupChatsRestControllerIT extends BaseIT {

    private static final String URI = "/api/v1/admin/chats";

    @SneakyThrows
    @Test
    @DisplayName("Should return empty list when no chats exists")
    @WithMockUser(authorities = "ADMIN")
    public void findAll_empty() {
        mockMvc.perform(get(URI))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    @SneakyThrows
    @Test
    @DisplayName("Should return 404 when chat with id not found")
    @WithMockUser(authorities = "ADMIN")
    public void getById_notFound() {
        mockMvc.perform(get(URI + "/{chatId}", chatIdCounter.get() - 1))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn()
                .getResponse();
    }

    @SneakyThrows
    @Test
    @DisplayName("Should return chat when chat with id found")
    @WithMockUser(authorities = "ADMIN")
    public void getById_success() {
        Long chatId = createTestChat("Chat");
        mockMvc.perform(get(URI + "/{chatId}", chatId))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        deleteTestChat(chatId);
    }
}
