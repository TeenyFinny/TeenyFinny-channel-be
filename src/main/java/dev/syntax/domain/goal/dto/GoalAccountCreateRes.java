package dev.syntax.domain.goal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoalAccountCreateRes {
    private boolean success;
}