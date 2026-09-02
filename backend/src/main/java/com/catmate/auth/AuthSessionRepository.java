package com.catmate.auth;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
    Optional<AuthSession> findByTokenAndExpiresAtAfter(String token, LocalDateTime now);
    void deleteByToken(String token);
}
