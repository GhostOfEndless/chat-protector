package ru.tbank.admin.service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.admin.config.security.JwtProperties;

import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ClaimsExtractorService {

    private final JwtProperties jwtProperties;

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        var claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtProperties.getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}