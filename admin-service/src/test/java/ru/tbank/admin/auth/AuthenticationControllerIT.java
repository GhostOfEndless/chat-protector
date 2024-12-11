package ru.tbank.admin.auth;

import lombok.SneakyThrows;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.tbank.admin.BaseIT;
import ru.tbank.admin.auth.payload.AuthenticationRequest;
import ru.tbank.admin.auth.payload.AuthenticationResponse;
import ru.tbank.admin.generated.tables.AppUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthenticationControllerIT extends BaseIT {

    private static final String URI = "/api/v1/auth/authenticate";
    private static final String URI_USERS = "/api/v1/admin/users";

    @Autowired
    private PasswordEncoder passwordEncoder;

    @SneakyThrows
    @Test
    @DisplayName("Should not authenticate not register user")
    public void authenticate_error() {
        var payload = AuthenticationRequest.builder()
                .login("login")
                .password("password")
                .build();
        mockMvc.perform(post(URI)
                        .content(objectMapper.writeValueAsString(payload))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isUnauthorized(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn()
                .getResponse();
    }

    @SneakyThrows
    @Test
    @DisplayName("Should not return 403 while payload is invalid")
    public void authenticate_badRequest() {
        var payload = AuthenticationRequest.builder()
                .build();
        mockMvc.perform(post(URI)
                        .content(objectMapper.writeValueAsString(payload))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn()
                .getResponse();
    }


    @SneakyThrows
    @Test
    @DisplayName("Should authenticate registered user")
    public void authenticate_success() {
        var payload = AuthenticationRequest.builder()
                .login("login")
                .password("password")
                .build();
        Long userId = createTestUser();
        var response = mockMvc.perform(post(URI)
                        .content(objectMapper.writeValueAsString(payload))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        var content = objectMapper.readValue(response.getContentAsString(), AuthenticationResponse.class);
        mockMvc.perform(get(URI_USERS)
                        .header("Authorization", "Bearer %s".formatted(content.token())))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        deleteTestUser(userId);
    }

    @SneakyThrows
    @Test
    @DisplayName("Should return 401 error due to token invalid")
    public void authenticate_unauthorized() {
        var payload = AuthenticationRequest.builder()
                .login("login")
                .password("password")
                .build();
        Long userId = createTestUser();
        mockMvc.perform(post(URI)
                        .content(objectMapper.writeValueAsString(payload))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        mockMvc.perform(get(URI_USERS)
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                                "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                                "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
                .andExpectAll(
                        status().isUnauthorized(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn()
                .getResponse();

        deleteTestUser(userId);
    }

    private @NonNull Long createTestUser() {
        var table = AppUser.APP_USER;
        Long userId = userIdCounter.incrementAndGet();
        dsl.insertInto(table)
                .columns(table.ID, table.FIRST_NAME, table.LAST_NAME, table.USERNAME, table.HASHED_PASSWORD, table.ROLE)
                .values(userId, "Name", "Surname", "login", passwordEncoder.encode("password"), "ADMIN")
                .execute();
        return userId;
    }
}
