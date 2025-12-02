package dev.syntax.domain.auth.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dev.syntax.domain.auth.entity.OtpRateLimit;

public interface OtpRateLimitRepository extends JpaRepository<OtpRateLimit, Long> {

	@Query("""
        SELECT COUNT(o)
        FROM OtpRateLimit o
        WHERE o.userId = :userId
          AND o.requestedAt >= :after
    """)
	long countRecentRequests(Long userId, LocalDateTime after);
}
