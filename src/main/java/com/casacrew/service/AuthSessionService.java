package com.casacrew.service;

import com.casacrew.model.AuthSession;
import com.casacrew.model.User;
import com.casacrew.repository.AuthSessionRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Service
@Transactional
public class AuthSessionService {

    private static final Logger log = LoggerFactory.getLogger(AuthSessionService.class);

    private final AuthSessionRepository authSessionRepository;
    private final UserRepository userRepository;
    private final long sessionExpirySeconds;

    public AuthSessionService(
            AuthSessionRepository authSessionRepository,
            UserRepository userRepository,
            @Value("${app.auth.session-expiry-seconds:3600}") long sessionExpirySeconds
    ) {
        this.authSessionRepository = authSessionRepository;
        this.userRepository = userRepository;
        this.sessionExpirySeconds = sessionExpirySeconds;
    }

    public record SessionResult(String token, Instant expiresAt) {}

    /**
     * Maakt een sessie aan voor een reeds geverifieerde gebruiker (na een
     * geslaagde wachtwoord- of Google-login). De caller heeft dus al
     * bewezen dat dit e-mailadres bij een echte, ingelogde gebruiker
     * hoort -- deze methode zoekt alleen de bijbehorende entity op.
     */
    public SessionResult createSession(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Gebruiker niet gevonden: " + email));

        String token = generateToken64Hex();
        Instant expiresAt = Instant.now().plusSeconds(sessionExpirySeconds);

        authSessionRepository.save(new AuthSession(token, user, user.getOrganization(), expiresAt));

        log.info("Sessie aangemaakt (userId={}, organizationId={})", user.getId(), user.getOrganization().getId());

        return new SessionResult(token, expiresAt);
    }

    public Optional<User> validateAndTouch(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        Optional<AuthSession> sessionOpt = authSessionRepository.findByToken(token.trim());
        if (sessionOpt.isEmpty()) {
            return Optional.empty();
        }

        AuthSession session = sessionOpt.get();
        if (!session.isActive()) {
            return Optional.empty();
        }

        session.touch();
        return Optional.of(session.getUser());
    }

    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        authSessionRepository.findByToken(token.trim()).ifPresent(AuthSession::revoke);
    }

    public void revokeAllForUser(User user) {
        if (user == null) {
            throw new AccessDeniedException("User is verplicht");
        }

        authSessionRepository.findByUser(user).forEach(AuthSession::revoke);
    }

    /**
     * Hard delete i.p.v. revoke -- nodig wanneer de User zelf wordt
     * verwijderd (auth_sessions.user_id heeft een NOT NULL FK).
     */
    public void deleteAllForUser(User user) {
        if (user == null) {
            return;
        }

        authSessionRepository.deleteAllByUser(user);
    }

    private static String generateToken64Hex() {
        byte[] bytes = new byte[32]; // 32 bytes = 64 hex chars
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
