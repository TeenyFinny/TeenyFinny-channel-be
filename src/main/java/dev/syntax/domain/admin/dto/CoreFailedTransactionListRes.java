package dev.syntax.domain.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Core 시스템의 실패 거래 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreFailedTransactionListRes {

    private Long id;
    private Long userId;
    private Long accountId;
    private String accountNumber;
    private String code;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String merchantName;
    private String category;
    private String status;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
}
