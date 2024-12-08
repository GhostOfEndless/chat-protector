package ru.tbank.admin.service.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.tbank.admin.auth.AuthenticationRequest;
import ru.tbank.admin.auth.AuthenticationResponse;
import ru.tbank.admin.entity.ApplicationUser;
import ru.tbank.admin.service.persistence.AppUserService;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AppUserService appUserService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse authenticate(@NonNull AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.login(),
                        request.password()
                )
        );
        ApplicationUser user = appUserService.getByUsername(request.login());
        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }
}
