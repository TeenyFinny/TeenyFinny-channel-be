package dev.syntax.domain.goal.dto;

import dev.syntax.domain.goal.entity.Goal;
import dev.syntax.global.service.Utils;
import lombok.Getter;

import java.math.RoundingMode;

@Getter
public class GoalPendingRes {
    private Long id;
    private String name;
    private String targetAmount;
    private String monthlyAmount;
    private Integer period;

    public GoalPendingRes(Goal goal) {
        this.id = goal.getId();
        this.name = goal.getName();
        this.targetAmount = Utils.NumberFormattingService(goal.getTargetAmount().intValue());
        this.monthlyAmount = Utils.NumberFormattingService(goal.getMonthlyAmount().intValue());
        
        // 기간 계산: 목표금액 / 월납입금 (올림 처리)
        if (goal.getMonthlyAmount().signum() > 0) {
            this.period = goal.getTargetAmount()
                    .divide(goal.getMonthlyAmount(), 0, RoundingMode.CEILING)
                    .intValue();
        } else {
            this.period = 0;
        }
    }
}
