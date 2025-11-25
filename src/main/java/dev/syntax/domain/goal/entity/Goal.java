package dev.syntax.domain.goal.entity;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.goal.enums.GoalStatus;
import dev.syntax.domain.user.entity.User;
import dev.syntax.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Goal
 *
 * <p>사용자의 목표 적금 정보를 나타내는 엔티티입니다.<br>
 * 목표 금액, 월 납입금, 납입일, 상태 값 등을 포함하며
 * 자녀가 생성하고 부모가 승인하는 형태의 목표 기반 저축 시스템에 사용됩니다.</p>
 */
@Entity
@Table(name = "goal")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal extends BaseTimeEntity {

    /**
     * 목표 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Long id;

    /**
     * 목표를 생성한 사용자(자녀)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    /**
     * 목표 이름
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 목표 금액
     */
    @Column(name = "target_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    /**
     * 월 납입 금액
     */
    @Column(name = "monthly_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyAmount;

    /**
     * 매월 자동 이체 납입일 (1~31)
     */
    @Column(name = "pay_day", nullable = false)
    private Integer payDay;

    /**
     * 목표 상태 (대기, 거절, 진행중, 완료, 취소)
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GoalStatus status = GoalStatus.PENDING;

    /**
     * 납입일 변경
     *
     * @param newPayDay 새 납입일
     */
    public void updatePayDay(Integer newPayDay) {
        this.payDay = newPayDay;
    }

    /**
     * 목표 상태 변경
     *
     * @param goalStatus 변경할 목표 상태
     */
    public void updateStatus(GoalStatus goalStatus) {
        this.status = goalStatus;
    }
}
