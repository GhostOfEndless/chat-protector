package ru.tbank.admin.auth;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String token
) {
}
