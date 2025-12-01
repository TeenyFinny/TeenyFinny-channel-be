package dev.syntax.domain.report.repository;

import dev.syntax.domain.report.entity.DetailReport;
import dev.syntax.domain.report.entity.SummaryReport;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 상세 리포트(카테고리별 지출) 레포지토리.
 */
public interface DetailReportRepository extends JpaRepository<DetailReport, Long> {

    /**
     * 요약 리포트 ID로 상세 리포트 목록을 조회합니다.
     *
     * @param reportId 요약 리포트 ID
     * @return 상세 리포트 목록
     */
    List<DetailReport> findByReport(SummaryReport report);

    void deleteByReport(SummaryReport report);
}
