package ru.tbank.admin.controller.users;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import ru.tbank.admin.BaseIT;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UsersRestControllerIT extends BaseIT {

    private static final String URI = "/api/v1/admin/users";

    @SneakyThrows
    @Test
    @DisplayName("Should return empty list when no users exists")
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
    @DisplayName("Should return 403 when param is invalid")
    @WithMockUser(authorities = "ADMIN")
    public void findAll_badRequest() {
        mockMvc.perform(get(URI)
                        .param("page", "-1"))
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn()
                .getResponse();
    }

    @SneakyThrows
    @Test
    @DisplayName("Should return 404 when user with id not found")
    @WithMockUser(authorities = "ADMIN")
    public void getById_notFound() {
        mockMvc.perform(get(URI + "/{userId}", userIdCounter.get() + 1))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn()
                .getResponse();
    }

    @SneakyThrows
    @Test
    @DisplayName("Should return user when user with id found")
    @WithMockUser(authorities = "ADMIN")
    public void getById_success() {
        Long userId = createTestUser("Name", "Surname", "username");
        mockMvc.perform(get(URI + "/{userId}", userId))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        deleteTestUser(userId);
    }
}
