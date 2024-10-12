package ru.tbank.admin.auth;

public record AuthenticationRequest(
        String login,
        String password
) {
}
