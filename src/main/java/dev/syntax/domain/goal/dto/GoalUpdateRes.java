package dev.syntax.domain.goal.dto;

import dev.syntax.domain.goal.entity.Goal;
import lombok.Getter;

@Getter
public class GoalUpdateRes {

    private Long goalId;
    private Integer payDay;

    public GoalUpdateRes(Goal goal) {
        this.goalId = goal.getId();
        this.payDay = goal.getPayDay();
    }
}
