package ru.tbank.admin.service.security;

import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.tbank.admin.config.security.JwtProperties;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String generateToken(UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            @NonNull UserDetails userDetails
    ) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.ttl().toMillis()))
                .signWith(jwtProperties.getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }
}
