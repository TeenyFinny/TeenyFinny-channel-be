package dev.syntax.domain.goal.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class GoalCreateReq {

    private String name;
    private BigDecimal targetAmount;
    private BigDecimal monthlyAmount;
    private Integer payDay;
}
