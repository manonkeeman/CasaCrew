package com.villavredestein.jobs;

import com.villavredestein.repository.AuthSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class AuthSessionCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(AuthSessionCleanupJob.class);

    private final AuthSessionRepository authSessionRepository;

    public AuthSessionCleanupJob(AuthSessionRepository authSessionRepository) {
        this.authSessionRepository = authSessionRepository;
    }

    @Scheduled(cron = "0 30 3 * * *", zone = "Europe/Amsterdam")
    @Transactional
    public void cleanupExpiredAndRevokedSessions() {
        authSessionRepository.deleteExpiredOrRevoked(Instant.now());
        log.info("AuthSessionCleanupJob: verlopen/ingetrokken sessies opgeruimd");
    }
}
