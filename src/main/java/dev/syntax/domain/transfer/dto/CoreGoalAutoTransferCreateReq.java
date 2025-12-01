package dev.syntax.domain.transfer.dto;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Core 목표 자동이체 생성 요청 DTO.
 *
 * <p>
 * 부모가 자녀의 용돈 계좌에서 자녀의 목표 계좌로 자동 이체를 생성할 때 사용됩니다.
 * </p>
 *
 * @param childCoreId 자녀 CoreUser ID
 * @param amount      이체 금액
 * @param transferDay 매월 실행일
 */
@Builder
public record CoreGoalAutoTransferCreateReq(
        Long childCoreId,
        BigDecimal amount,
        Integer transferDay
) {
}
