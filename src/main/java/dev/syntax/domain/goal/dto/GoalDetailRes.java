package dev.syntax.domain.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class GoalDetailRes {

    private Long goalId;
    private Long userId;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private int period;
    private int progress;
    private String userName;
    private List<BigDecimal> depositAmount;
    private List<String> depositDatetime;
}
