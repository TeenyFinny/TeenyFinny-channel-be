package dev.syntax.domain.goal.dto;

import dev.syntax.domain.goal.entity.Goal;
import lombok.Getter;

@Getter
public class GoalCreateRes {

    private Long goalId;

    public GoalCreateRes(Goal goal) {
        this.goalId = goal.getId();
    }
}
