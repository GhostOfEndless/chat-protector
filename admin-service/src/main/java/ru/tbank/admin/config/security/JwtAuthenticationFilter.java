package ru.tbank.admin.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.tbank.admin.service.security.JwtService;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_TYPE = "Bearer ";
    private final UserDetailsService userDetailsService;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(BEARER_TYPE)) {
                filterChain.doFilter(request, response);
                return;
            }

            var jwt = authHeader.substring(BEARER_TYPE.length());
            var userLogin = jwtService.extractUsername(jwt);

            if (userLogin != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                setAuthentication(jwt, request);
            } else if (userLogin == null) {
                throw new IllegalArgumentException();
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            addProblemDetailToResponse(request, response, "token.expired", null);
        } catch (IllegalArgumentException e) {
            addProblemDetailToResponse(request, response, "token.login_field_not_found", null);
        } catch (UsernameNotFoundException e) {
            addProblemDetailToResponse(request, response, "username.not_found", new Object[]{e.getMessage()});
        } catch (SignatureException e) {
            addProblemDetailToResponse(request, response, "token.signature_invalid", null);
        }
    }

    private void addProblemDetailToResponse(HttpServletRequest request, HttpServletResponse response, String detail,
                                            Object[] args) throws IOException {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                messageSource.getMessage("errors.401.title", new Object[0],
                        "errors.401.title", request.getLocale()));

        problemDetail.setProperty("error", messageSource.getMessage(detail, args,
                detail, request.getLocale()));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }

    private void setAuthentication(String jwt, HttpServletRequest request) {
        if (!jwtService.isTokenExpired(jwt)) {
            var userDetails = userDetailsService.loadUserByUsername(jwtService.extractUsername(jwt));
            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
}