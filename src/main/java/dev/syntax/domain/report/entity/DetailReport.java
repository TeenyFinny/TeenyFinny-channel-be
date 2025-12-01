package dev.syntax.domain.report.entity;

import dev.syntax.domain.report.enums.Category;
import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "detail_report")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private SummaryReport report;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 100)
    private Category category;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal percent;
}
