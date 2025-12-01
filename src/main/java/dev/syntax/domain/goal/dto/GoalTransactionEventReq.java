package dev.syntax.domain.goal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class GoalTransactionEventReq {
    private String accountNo;
    private BigDecimal balanceAfter;
    private BigDecimal transactionAmount;
    private String transactionType; // "DEPOSIT" or "WITHDRAWAL"
}
