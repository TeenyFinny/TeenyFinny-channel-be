package dev.syntax.domain.account.dto.core;

import java.math.BigDecimal;
import java.util.List;

/**
 * 코어 서버의 월별 거래내역 조회 응답 DTO
 * <p>
 * 특정 계좌의 년/월별 거래 내역과 현재 잔액을 포함합니다.
 * </p>
 *
 * @param items 거래 내역 리스트
 * @param balance 현재 계좌 잔액
 */
public record CoreTransactionHistoryRes(
        List<CoreTransactionItemRes> transactions,
        BigDecimal balance
) {
}
