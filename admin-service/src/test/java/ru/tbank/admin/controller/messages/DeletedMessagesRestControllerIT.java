package ru.tbank.admin.controller.messages;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import ru.tbank.admin.BaseIT;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DeletedMessagesRestControllerIT extends BaseIT {

    private static final String URI = "/api/v1/admin/chats/{chatId}/deleted-messages";

    @SneakyThrows
    @Test
    @DisplayName("Should return empty list when no deleted messages exists")
    @WithMockUser(authorities = "ADMIN")
    public void findAll_empty() {
        Long chatId = createTestChat("Chat");
        mockMvc.perform(get(URI, chatId))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        deleteTestChat(chatId);
    }

    @SneakyThrows
    @Test
    @DisplayName("Should return 404 when chat not found")
    @WithMockUser(authorities = "ADMIN")
    public void findAll_invalidChatId() {
        mockMvc.perform(get(URI, chatIdCounter.get() - 1))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn()
                .getResponse();
    }

    @SneakyThrows
    @Test
    @DisplayName("Should return 404 when user not found")
    @WithMockUser(authorities = "ADMIN")
    public void findAll_invalidUserId() {
        Long chatId = createTestChat("Chat");
        mockMvc.perform(get(URI, -1)
                        .param("userId", "2"))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn()
                .getResponse();
        deleteTestChat(chatId);
    }

    @SneakyThrows
    @Test
    @DisplayName("Should return deleted messages of user")
    @WithMockUser(authorities = "ADMIN")
    public void findAll_withUserId() {
        Long chatId = createTestChat("Test chat");
        Long userId = createTestUser("Name", "Surname", "username");
        mockMvc.perform(get(URI, -1)
                        .param("userId", "1"))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn()
                .getResponse();
        deleteTestChat(chatId);
        deleteTestUser(userId);
    }
}
