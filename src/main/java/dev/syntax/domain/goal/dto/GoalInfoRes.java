package dev.syntax.domain.goal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.syntax.domain.goal.entity.Goal;
import dev.syntax.global.service.Utils;
import lombok.Getter;

@Getter
public class GoalInfoRes {

    private Long id;
    private String name;
    private String targetAmount;
    private String monthlyAmount;
    private Integer payDay;

    public GoalInfoRes(Goal goal) {
        this.id = goal.getId();
        this.name = goal.getName();
        this.targetAmount = Utils.NumberFormattingService(goal.getTargetAmount().intValue());
        this.monthlyAmount = Utils.NumberFormattingService(goal.getMonthlyAmount().intValue());
        this.payDay = goal.getPayDay();
    }
}
