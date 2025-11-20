package dev.syntax.domain.report.repository;

import dev.syntax.domain.report.entity.SummaryReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryReportRepository extends JpaRepository<SummaryReport, Long> {
}
