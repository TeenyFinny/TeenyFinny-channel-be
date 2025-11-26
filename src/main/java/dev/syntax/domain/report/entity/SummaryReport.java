package dev.syntax.domain.report.entity;

import dev.syntax.domain.user.entity.User;
import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "summary_report")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "total_expense", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalExpense;

    @Column(name = "prev_total_expense", nullable = true, precision = 12, scale = 2)
    private BigDecimal prevTotalExpense;
}
