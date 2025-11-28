package dev.syntax.domain.account.dto.core;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 코어 서버의 거래 항목 DTO
 * <p>
 * 거래 내역 목록에서 사용되는 기본 거래 정보를 포함합니다.
 * </p>
 *
 * @param id 거래 ID
 * @param merchantName 거래처 이름
 * @param amount 거래 금액 (양수 = 입금, 음수 = 출금)
 * @param transactionDate 거래 일시
 * @param balanceAfter 거래 후 잔액
 */
public record CoreTransactionItemRes(
        Long transactionId,
        String merchantName,
        BigDecimal amount,
        LocalDateTime transactionDate,
        BigDecimal balanceAfter
) {
}
