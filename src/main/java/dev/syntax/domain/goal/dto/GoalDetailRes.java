package dev.syntax.domain.goal.dto;

import dev.syntax.domain.goal.entity.Goal;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class GoalDetailRes {

    private Long id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal monthlyAmount;
    private Integer payDay;

    public GoalDetailRes(Goal goal) {
        this.id = goal.getId();
        this.name = goal.getName();
        this.targetAmount = goal.getTargetAmount();
        this.monthlyAmount = goal.getMonthlyAmount();
        this.payDay = goal.getPayDay();
    }
}
