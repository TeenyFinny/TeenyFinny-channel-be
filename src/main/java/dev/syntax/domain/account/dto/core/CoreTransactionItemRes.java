package dev.syntax.domain.account.dto.core;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import dev.syntax.domain.report.enums.Category;


/**
 * 단일 거래 내역을 표현하는 DTO (Core 서버 응답)
 * <p>
 * 거래 시점, 거래 금액, 거래처, 거래 후 잔액 등
 * 하나의 거래 정보를 담아 반환합니다.
 * </p>
 *
 * @param transactionId 거래 엔티티 ID
 * @param merchantName 거래처 이름 (예: "편의점", "카페")
 * @param amount 거래 금액 
 * @param code 거래 코드 (WITHDRAW, DEPOSIT 등)
 * @param transactionDate 거래 일시
 * @param category 거래 카테고리 (FOOD, TRANSPORT, ETC 등)
 * @param balanceAfter 거래 후 잔액
 * @param transactionType 거래 유형 (예: 일시불, 할부)
 */
public record CoreTransactionItemRes(
        Long transactionId,
        String merchantName,
        BigDecimal amount,
        String code,
        LocalDateTime transactionDate,
        Category category,
        BigDecimal balanceAfter,
        String transactionType
) {
}
