package dev.syntax.domain.goal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GoalUpdateReq {
    @NotNull(message = "납입일은 필수입니다.")
    private Integer payDay;
}
