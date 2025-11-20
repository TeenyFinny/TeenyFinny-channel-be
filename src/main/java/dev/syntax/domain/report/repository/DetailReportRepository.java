package dev.syntax.domain.report.repository;

import dev.syntax.domain.report.entity.DetailReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetailReportRepository extends JpaRepository<DetailReport, Long> {
}
