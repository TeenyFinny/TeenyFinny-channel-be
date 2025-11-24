package dev.syntax.domain.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoalDeleteRes {
    private Long goalId;
    private String message;
}

