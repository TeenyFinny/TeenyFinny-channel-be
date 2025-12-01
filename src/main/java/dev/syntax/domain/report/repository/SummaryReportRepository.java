package dev.syntax.domain.report.repository;

import dev.syntax.domain.report.entity.SummaryReport;
import dev.syntax.domain.user.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 월간 금융 리포트(요약) 레포지토리.
 */
public interface SummaryReportRepository extends JpaRepository<SummaryReport, Long> {

    /**
     * 사용자 ID와 월(month)로 리포트를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param month  조회할 월 (1~12)
     * @return 해당 월의 리포트 (Optional)
     */
    Optional<SummaryReport> findByUserAndYearAndMonth(User user, int year, int month);

    @Query("SELECT s FROM SummaryReport s WHERE s.user.id = :userId AND (s.year < :year OR (s.year = :year AND s.month < :month))")
    java.util.List<SummaryReport> findOldReports(
        @Param("userId") Long userId,
        @Param("year") int year,
        @Param("month") int month
    );
}
