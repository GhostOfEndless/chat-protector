package ru.tbank.admin.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.tbank.admin.auth.AuthenticationRequest;
import ru.tbank.admin.auth.AuthenticationResponse;
import ru.tbank.admin.entity.AppUser;
import ru.tbank.admin.repository.AppUserRepository;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AppUserRepository repository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.login(),
                        request.password()
                )
        );
        AppUser user = repository.findByLogin(request.login()).orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }
}
