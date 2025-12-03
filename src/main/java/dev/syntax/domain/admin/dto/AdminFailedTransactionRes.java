package dev.syntax.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 클라이언트에 전달할 실패 거래 정보 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminFailedTransactionRes {

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

    public static AdminFailedTransactionRes from(CoreFailedTransactionListRes core) {
        return AdminFailedTransactionRes.builder()
                .id(core.getId())
                .userId(core.getUserId())
                .accountId(core.getAccountId())
                .accountNumber(core.getAccountNumber())
                .code(core.getCode())
                .type(core.getType())
                .amount(core.getAmount())
                .balanceAfter(core.getBalanceAfter())
                .merchantName(core.getMerchantName())
                .category(core.getCategory())
                .status(core.getStatus())
                .transactionDate(core.getTransactionDate())
                .createdAt(core.getCreatedAt())
                .build();
    }
}
