package dev.syntax.domain.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class GoalAccountInfoDto {

    private BigDecimal currentAmount;
    private List<BigDecimal> depositAmounts;
    private List<String> depositTimes;
}
