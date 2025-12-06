package dev.syntax.domain.feedback.repository;

import dev.syntax.domain.feedback.entity.Feedback;
import dev.syntax.domain.report.entity.SummaryReport;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    /**
     * 특정 리포트에 대한 피드백 존재 여부를 확인합니다.
     *
     * @param report 확인할 리포트 엔티티
     * @return 피드백 존재 시 true, 없으면 false
     */
    boolean existsByReport(SummaryReport report);

    /**
     * 특정 리포트에 대한 피드백을 조회합니다.
     *
     * @param report 조회할 리포트 엔티티
     * @return 피드백 Optional 객체
     */
    Optional<Feedback> findByReport(SummaryReport report);

    /**
     * 특정 리포트에 대한 피드백을 삭제합니다.
     *
     * @param report 삭제할 피드백의 리포트 엔티티
     */
    void deleteByReport(SummaryReport report);
}
