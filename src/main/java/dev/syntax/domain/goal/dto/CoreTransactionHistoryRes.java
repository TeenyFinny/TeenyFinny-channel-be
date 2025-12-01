package dev.syntax.domain.goal.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CoreTransactionHistoryRes {

    private BigDecimal balance;
    private List<TransactionItem> transactions;

    @Getter
    public static class TransactionItem {
        private BigDecimal amount;
        private String merchantName;
        private LocalDateTime transactionDate;
    }
}
