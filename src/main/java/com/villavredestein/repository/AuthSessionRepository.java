package com.villavredestein.repository;

import com.villavredestein.model.AuthSession;
import com.villavredestein.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {

    Optional<AuthSession> findByToken(String token);

    List<AuthSession> findByUser(User user);

    void deleteAllByUser(User user);

    @Modifying
    @Query("DELETE FROM AuthSession s WHERE s.expiresAt < :cutoff OR s.revokedAt IS NOT NULL")
    void deleteExpiredOrRevoked(@Param("cutoff") Instant cutoff);
}
