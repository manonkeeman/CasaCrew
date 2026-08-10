package com.villavredestein.service;

import com.villavredestein.dto.ResetPasswordRequestDTO;
import com.villavredestein.model.PasswordResetToken;
import com.villavredestein.model.User;
import com.villavredestein.repository.PasswordResetTokenRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final long resetExpiryMinutes;
    private final String frontendUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService,
            @Value("${app.password-reset.expiry-minutes:30}") long resetExpiryMinutes,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.resetExpiryMinutes = resetExpiryMinutes;
        this.frontendUrl = frontendUrl;
    }

    public void createResetTokenIfUserExists(String emailRaw) {
        String email = normalizeEmail(emailRaw);
        if (email.isBlank()) return;

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();

        String token = generateToken64Hex();
        Instant expiresAt = Instant.now().plus(resetExpiryMinutes, ChronoUnit.MINUTES);

        tokenRepository.save(new PasswordResetToken(token, user, expiresAt));

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        mailService.sendPasswordResetMail(user.getEmail(), resetLink);
    }

    public void resetPassword(ResetPasswordRequestDTO dto) {
        String token = dto.token();
        String newPassword = dto.newPassword();

        PasswordResetToken prt = tokenRepository.findByToken(token.trim())
                .orElseThrow(() -> new EntityNotFoundException("Invalid or unknown token"));

        if (prt.isUsed()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Token already used");
        if (prt.isExpired()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Token expired");

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        prt.markUsed();
    }

    private String normalizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String generateToken64Hex() {
        byte[] bytes = new byte[32]; // 32 bytes = 64 hex chars
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}