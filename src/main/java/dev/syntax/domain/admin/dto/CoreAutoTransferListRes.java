package dev.syntax.domain.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Core 시스템의 자동이체 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreAutoTransferListRes {

    private Long id;
    private Long userId;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private String memo;
    private Integer transferDay;
    private LocalDate nextTransferDay;
    private String status;
    private LocalDateTime createdAt;
}
