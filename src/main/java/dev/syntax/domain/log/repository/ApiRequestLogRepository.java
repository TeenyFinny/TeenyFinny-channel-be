package dev.syntax.domain.log.repository;

import dev.syntax.domain.log.entity.ApiRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiRequestLogRepository extends JpaRepository<ApiRequestLog, Long> {
}
