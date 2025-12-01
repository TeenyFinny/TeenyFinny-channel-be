package dev.syntax.domain.report.entity;

import java.math.BigDecimal;

import dev.syntax.domain.user.entity.User;
import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "total_expense", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalExpense;

    @Column(name = "prev_total_expense", precision = 12, scale = 2)
    private BigDecimal prevTotalExpense;
}
