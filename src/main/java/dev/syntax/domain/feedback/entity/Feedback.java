package dev.syntax.domain.feedback.entity;

import dev.syntax.domain.report.entity.SummaryReport;
import dev.syntax.domain.user.entity.User;
import dev.syntax.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feedback")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private SummaryReport report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
}
