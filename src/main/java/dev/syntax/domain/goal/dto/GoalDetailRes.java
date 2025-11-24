package dev.syntax.domain.goal.dto;

import dev.syntax.global.service.Utils;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class GoalDetailRes {

    private Long goalId;
    private Long userId;
    private String name;
    private String targetAmount;
    private String currentAmount;
    private int period;
    private int progress;
    private String userName;
    private List<String> depositAmount;
    private List<String> depositDatetime;

    public GoalDetailRes(
            Long goalId,
            Long userId,
            String name,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            int period,
            int progress,
            String userName,
            List<BigDecimal> depositAmount,
            List<String> depositDatetime
    ) {
        this.goalId = goalId;
        this.userId = userId;
        this.name = name;
        this.targetAmount = Utils.NumberFormattingService(targetAmount.intValue());
        this.currentAmount = Utils.NumberFormattingService(currentAmount.intValue());
        this.period = period;
        this.progress = progress;
        this.userName = userName;
        this.depositAmount = depositAmount.stream()
                .map(v -> Utils.NumberFormattingService(v.intValue()))
                .collect(Collectors.toList());

        this.depositDatetime = depositDatetime;
    }
}
