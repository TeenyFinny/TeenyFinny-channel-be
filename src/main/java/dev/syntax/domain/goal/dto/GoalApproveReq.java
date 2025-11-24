package dev.syntax.domain.goal.dto;

import lombok.Getter;

@Getter
public class GoalApproveReq {
    private boolean approve;   // true → 승인, false → 거절
}
