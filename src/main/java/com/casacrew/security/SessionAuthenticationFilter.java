package com.casacrew.security;

import com.casacrew.model.User;
import com.casacrew.service.AuthSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    public static final String SESSION_TOKEN_ATTRIBUTE = "authSessionToken";

    private static final Logger log = LoggerFactory.getLogger(SessionAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private static final Set<String> PUBLIC_AUTH_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/google-login",
            "/api/auth/forgot-password",
            "/api/auth/reset-password"
    );

    private final AuthSessionService authSessionService;
    private final UserDetailsService userDetailsService;

    public SessionAuthenticationFilter(AuthSessionService authSessionService, UserDetailsService userDetailsService) {
        this.authSessionService = authSessionService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (isSkippableRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<User> userOpt = validateSafely(token);
        if (userOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails = loadUserSafely(userOpt.get().getEmail());
        if (userDetails == null) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        request.setAttribute(SESSION_TOKEN_ATTRIBUTE, token);

        filterChain.doFilter(request, response);
    }

    private boolean isSkippableRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return "OPTIONS".equalsIgnoreCase(method)
                || PUBLIC_AUTH_PATHS.contains(path)
                || path.startsWith("/h2-console")
                || path.startsWith("/error")
                || path.startsWith("/actuator/health")
                || path.startsWith("/actuator/healthz")
                || path.startsWith("/actuator/info");
    }

    private Optional<User> validateSafely(String token) {
        try {
            return authSessionService.validateAndTouch(token);
        } catch (Exception exception) {
            log.warn("Session validation failed: {}", exception.getMessage());
            return Optional.empty();
        }
    }

    private UserDetails loadUserSafely(String email) {
        try {
            return userDetailsService.loadUserByUsername(email);
        } catch (Exception exception) {
            log.warn("User referenced in session no longer exists: {}", maskEmail(email));
            return null;
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "(no-email)";
        }

        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');

        if (atIndex <= 1) {
            return "***";
        }

        return trimmed.charAt(0) + "***" + trimmed.substring(atIndex);
    }
}
