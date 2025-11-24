package dev.syntax.domain.goal.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class GoalCreateReq {

    @NotBlank(message = "목표 이름은 비워둘 수 없습니다.")
    private String name;

    @NotNull(message = "목표 금액은 비워둘 수 없습니다.")
    @Positive(message = "목표 금액은 0보다 커야 합니다.")
    private BigDecimal targetAmount;

    @NotNull(message = "월 납입 금액은 비워둘 수 없습니다.")
    @Positive(message = "월 납입 금액은 0보다 커야 합니다.")
    private BigDecimal monthlyAmount;

    @NotNull(message = "납입일은 비워둘 수 없습니다.")
    private Integer payDay;
}
