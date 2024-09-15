package org.example.adminpanel.security;

import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.adminpanel.entity.AppUser;
import org.example.adminpanel.repository.AppUserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthenticatedUser {

    private final AppUserRepository repository;
    private final AuthenticationContext authenticationContext;

    private static final String JWT_HEADER_AND_PAYLOAD_COOKIE_NAME = "jwt.headerAndPayload";
    private static final String JWT_SIGNATURE_COOKIE_NAME = "jwt.signature";

    @Transactional
    public Optional<AppUser> get() {
        return this.authenticationContext.getAuthenticatedUser(Jwt.class)
                .map(jwt -> {
                    var username = jwt.getClaimAsString("sub");
                    return this.repository.findByUsername(username).orElseThrow();
                });
    }

    public void logout() {
        this.authenticationContext.logout();
        clearCookies();
    }

    private void clearCookies() {
        clearCookie(JWT_HEADER_AND_PAYLOAD_COOKIE_NAME);
        clearCookie(JWT_SIGNATURE_COOKIE_NAME);
    }

    private void clearCookie(String cookieName) {
        var request = VaadinServletRequest.getCurrent().getHttpServletRequest();
        var response = VaadinServletResponse.getCurrent().getHttpServletResponse();

        var cookie = new Cookie(cookieName, null);
        cookie.setPath(getRequestContextPath(request));
        cookie.setMaxAge(0);
        cookie.setSecure(request.isSecure());
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
    }

    private String getRequestContextPath(HttpServletRequest request) {
        var contextPath = request.getContextPath();
        return "".equals(contextPath) ? "/" : contextPath;
    }
}
