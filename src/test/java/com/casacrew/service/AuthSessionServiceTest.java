package com.casacrew.service;

import com.casacrew.model.AuthSession;
import com.casacrew.model.Organization;
import com.casacrew.model.User;
import com.casacrew.repository.AuthSessionRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthSessionServiceTest {

    private static final Long ORG_ID = 1L;
    private static final long EXPIRY_SECONDS = 3600L;

    @Mock AuthSessionRepository authSessionRepository;
    @Mock UserRepository userRepository;

    AuthSessionService authSessionService;

    @BeforeEach
    void setUp() {
        authSessionService = new AuthSessionService(authSessionRepository, userRepository, EXPIRY_SECONDS);
    }

    private Organization makeOrganization() {
        Organization organization = new Organization("CasaCrew", "casacrew");
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        return organization;
    }

    private User makeUser(long id, String email) {
        User user = new User("student", email, "hash", User.Role.STUDENT);
        user.setOrganization(makeOrganization());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private AuthSession makeSession(String token, User user, Instant expiresAt, boolean revoked) {
        AuthSession session = new AuthSession(token, user, user.getOrganization(), expiresAt);
        if (revoked) {
            session.revoke();
        }
        return session;
    }


    @Test
    void createSession_userFound_savesSessionAndReturnsTokenWithExpiry() {
        User user = makeUser(1L, "s@test.com");
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(user));

        AuthSessionService.SessionResult result = authSessionService.createSession("s@test.com");

        assertThat(result.token()).isNotBlank();
        assertThat(result.token()).hasSize(64);
        assertThat(result.expiresAt()).isAfter(Instant.now());
        verify(authSessionRepository).save(any(AuthSession.class));
    }

    @Test
    void createSession_generatesDifferentTokenEachTime() {
        User user = makeUser(1L, "s@test.com");
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(user));

        String token1 = authSessionService.createSession("s@test.com").token();
        String token2 = authSessionService.createSession("s@test.com").token();

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void createSession_userNotFound_throwsEntityNotFoundException() {
        when(userRepository.findByEmailIgnoreCase("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authSessionService.createSession("missing@test.com"));
        verify(authSessionRepository, never()).save(any());
    }


    @Test
    void validateAndTouch_activeSession_returnsUserAndTouches() {
        User user = makeUser(1L, "s@test.com");
        AuthSession session = makeSession("tok123", user, Instant.now().plusSeconds(60), false);
        when(authSessionRepository.findByToken("tok123")).thenReturn(Optional.of(session));

        Optional<User> result = authSessionService.validateAndTouch("tok123");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("s@test.com");
        assertThat(session.getLastUsedAt()).isNotNull();
    }

    @Test
    void validateAndTouch_expiredSession_returnsEmpty() {
        User user = makeUser(1L, "s@test.com");
        AuthSession session = makeSession("tok123", user, Instant.now().minusSeconds(60), false);
        when(authSessionRepository.findByToken("tok123")).thenReturn(Optional.of(session));

        Optional<User> result = authSessionService.validateAndTouch("tok123");

        assertThat(result).isEmpty();
    }

    @Test
    void validateAndTouch_revokedSession_returnsEmpty() {
        User user = makeUser(1L, "s@test.com");
        AuthSession session = makeSession("tok123", user, Instant.now().plusSeconds(60), true);
        when(authSessionRepository.findByToken("tok123")).thenReturn(Optional.of(session));

        Optional<User> result = authSessionService.validateAndTouch("tok123");

        assertThat(result).isEmpty();
    }

    @Test
    void validateAndTouch_unknownToken_returnsEmpty() {
        when(authSessionRepository.findByToken("unknown")).thenReturn(Optional.empty());

        Optional<User> result = authSessionService.validateAndTouch("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void validateAndTouch_blankToken_returnsEmptyWithoutRepositoryCall() {
        Optional<User> result = authSessionService.validateAndTouch("   ");

        assertThat(result).isEmpty();
        verify(authSessionRepository, never()).findByToken(any());
    }

    @Test
    void validateAndTouch_nullToken_returnsEmptyWithoutRepositoryCall() {
        Optional<User> result = authSessionService.validateAndTouch(null);

        assertThat(result).isEmpty();
        verify(authSessionRepository, never()).findByToken(any());
    }


    @Test
    void revoke_knownToken_revokesSession() {
        User user = makeUser(1L, "s@test.com");
        AuthSession session = makeSession("tok123", user, Instant.now().plusSeconds(60), false);
        when(authSessionRepository.findByToken("tok123")).thenReturn(Optional.of(session));

        authSessionService.revoke("tok123");

        assertThat(session.isRevoked()).isTrue();
    }

    @Test
    void revoke_unknownToken_doesNothing() {
        when(authSessionRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authSessionService.revoke("unknown"));
    }

    @Test
    void revoke_blankToken_doesNotHitRepository() {
        authSessionService.revoke("  ");

        verify(authSessionRepository, never()).findByToken(any());
    }


    @Test
    void revokeAllForUser_revokesEverySessionOfUser() {
        User user = makeUser(1L, "s@test.com");
        AuthSession s1 = makeSession("tok1", user, Instant.now().plusSeconds(60), false);
        AuthSession s2 = makeSession("tok2", user, Instant.now().plusSeconds(60), false);
        when(authSessionRepository.findByUser(user)).thenReturn(List.of(s1, s2));

        authSessionService.revokeAllForUser(user);

        assertThat(s1.isRevoked()).isTrue();
        assertThat(s2.isRevoked()).isTrue();
    }

    @Test
    void revokeAllForUser_nullUser_throwsAccessDeniedException() {
        assertThrows(AccessDeniedException.class, () -> authSessionService.revokeAllForUser(null));
        verify(authSessionRepository, never()).findByUser(any());
    }


    @Test
    void deleteAllForUser_validUser_deletesAllSessions() {
        User user = makeUser(1L, "s@test.com");

        authSessionService.deleteAllForUser(user);

        verify(authSessionRepository).deleteAllByUser(user);
    }

    @Test
    void deleteAllForUser_nullUser_doesNothing() {
        authSessionService.deleteAllForUser(null);

        verify(authSessionRepository, never()).deleteAllByUser(any());
    }
}
