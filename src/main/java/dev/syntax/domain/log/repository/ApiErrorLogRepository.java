package dev.syntax.domain.log.repository;

import dev.syntax.domain.log.entity.ApiErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiErrorLogRepository extends JpaRepository<ApiErrorLog, Long> {
}
