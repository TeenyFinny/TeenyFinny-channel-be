package dev.syntax.domain.goal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Core 서버 → Channel 서버로 전달되는 목표 계좌 입금 이벤트 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalDepositEventReq {

    private String accountNo;
    private BigDecimal balanceAfter;
}
