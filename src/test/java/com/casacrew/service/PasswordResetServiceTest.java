package com.casacrew.service;

import com.casacrew.dto.ResetPasswordRequestDTO;
import com.casacrew.model.Organization;
import com.casacrew.model.PasswordResetToken;
import com.casacrew.model.User;
import com.casacrew.repository.PasswordResetTokenRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    private static final Long ORG_ID = 1L;
    private static final long EXPIRY_MINUTES = 30L;
    private static final String FRONTEND_URL = "http://localhost:5173";

    @Mock UserRepository userRepository;
    @Mock PasswordResetTokenRepository tokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock MailService mailService;

    PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                userRepository, tokenRepository, passwordEncoder, mailService, EXPIRY_MINUTES, FRONTEND_URL);
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


    @Test
    void createResetTokenIfUserExists_knownEmail_savesTokenAndSendsMail() {
        User user = makeUser(1L, "s@test.com");
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(user));

        passwordResetService.createResetTokenIfUserExists("S@Test.com");

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());
        PasswordResetToken saved = captor.getValue();
        assertThat(saved.getToken()).hasSize(64);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());

        verify(mailService).sendPasswordResetMail(eq("s@test.com"), contains(saved.getToken()));
    }

    /**
     * organization_id is NOT NULL in the database (see V5__organization_id_not_null_and_fk.sql),
     * but PasswordResetService#createResetTokenIfUserExists never calls
     * PasswordResetToken#setOrganization before saving. This asserts the intended/safe
     * behavior and is expected to FAIL against the current implementation -- see the bug
     * note in the test-writing report.
     */
    @Test
    void createResetTokenIfUserExists_savedToken_hasOrganizationSet() {
        User user = makeUser(1L, "s@test.com");
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(user));

        passwordResetService.createResetTokenIfUserExists("s@test.com");

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getOrganization())
                .as("organization_id is NOT NULL in the DB; PasswordResetToken must have an organization set before save")
                .isNotNull();
    }

    @Test
    void createResetTokenIfUserExists_unknownEmail_doesNothing() {
        when(userRepository.findByEmailIgnoreCase("missing@test.com")).thenReturn(Optional.empty());

        passwordResetService.createResetTokenIfUserExists("missing@test.com");

        verify(tokenRepository, never()).save(any());
        verify(mailService, never()).sendPasswordResetMail(any(), any());
    }

    @Test
    void createResetTokenIfUserExists_blankEmail_doesNothing() {
        passwordResetService.createResetTokenIfUserExists("   ");

        verify(userRepository, never()).findByEmailIgnoreCase(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void createResetTokenIfUserExists_nullEmail_doesNothing() {
        passwordResetService.createResetTokenIfUserExists(null);

        verify(userRepository, never()).findByEmailIgnoreCase(any());
        verify(tokenRepository, never()).save(any());
    }


    @Test
    void resetPassword_validToken_updatesPasswordAndMarksTokenUsed() {
        User user = makeUser(1L, "s@test.com");
        PasswordResetToken token = new PasswordResetToken("tok123", user, Instant.now().plusSeconds(600));
        when(tokenRepository.findByToken("tok123")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newSecret1")).thenReturn("encoded-hash");

        passwordResetService.resetPassword(new ResetPasswordRequestDTO("tok123", "newSecret1"));

        assertThat(user.getPassword()).isEqualTo("encoded-hash");
        assertThat(token.isUsed()).isTrue();
    }

    @Test
    void resetPassword_unknownToken_throwsEntityNotFoundException() {
        when(tokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> passwordResetService.resetPassword(new ResetPasswordRequestDTO("unknown", "newSecret1")));
    }

    @Test
    void resetPassword_alreadyUsedToken_throwsResponseStatusException() {
        User user = makeUser(1L, "s@test.com");
        PasswordResetToken token = new PasswordResetToken("tok123", user, Instant.now().plusSeconds(600));
        token.markUsed();
        when(tokenRepository.findByToken("tok123")).thenReturn(Optional.of(token));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> passwordResetService.resetPassword(new ResetPasswordRequestDTO("tok123", "newSecret1")));
        assertThat(ex.getStatusCode().value()).isEqualTo(422);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void resetPassword_expiredToken_throwsResponseStatusException() {
        User user = makeUser(1L, "s@test.com");
        PasswordResetToken token = new PasswordResetToken("tok123", user, Instant.now().minusSeconds(60));
        when(tokenRepository.findByToken("tok123")).thenReturn(Optional.of(token));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> passwordResetService.resetPassword(new ResetPasswordRequestDTO("tok123", "newSecret1")));
        assertThat(ex.getStatusCode().value()).isEqualTo(422);
        verify(passwordEncoder, never()).encode(any());
    }
}
