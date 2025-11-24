package dev.syntax.domain.goal.dto;

import dev.syntax.domain.goal.entity.Goal;
import dev.syntax.domain.goal.enums.GoalStatus;
import lombok.Getter;

@Getter
public class GoalApproveRes {

    private Long id;
    private GoalStatus status;

    public GoalApproveRes(Goal goal) {
        this.id = goal.getId();
        this.status = goal.getStatus();
    }
}
