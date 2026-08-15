package com.casacrew.repository;

import com.casacrew.model.PasswordResetToken;
import com.casacrew.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    List<PasswordResetToken> findAllByExpiresAtBefore(Instant time);
    void deleteAllByUser(User user);
}