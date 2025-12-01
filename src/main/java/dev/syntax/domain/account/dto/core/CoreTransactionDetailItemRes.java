package dev.syntax.domain.account.dto.core;

import dev.syntax.domain.report.enums.Category;

/**
 * 상세 거래 내역을 표현하는 DTO (Core 서버 응답)
 * <p>
 * 기본 거래 정보에 더해 거래 타입, 카테고리, 승인 금액 등
 * 추가 정보를 포함하여 반환합니다.
 * 모든 금액과 날짜는 포맷된 문자열로 반환됩니다.
 * </p>
 *
 * @param merchantName 거래처 이름 (예: "스타벅스", "편의점")
 * @param amount 거래 금액 (포맷: "5,300")
 * @param transactionDate 거래 일시 (포맷: "yyyy.MM.dd HH:mm:ss")
 * @param type 결제 방식 (PAY_IN_FULL, INSTALLMENT 등, nullable)
 * @param category 거래 카테고리 (FOOD, TRANSFER, SALARY 등)
 * @param approveAmount 승인 금액 (포맷: "5,300")
 * @param balanceAfter 거래 후 잔액 (포맷: "5,300")
 * @param code 거래 코드 (WITHDRAW, DEPOSIT 등)
 */
public record CoreTransactionDetailItemRes(
        String merchantName,
        String amount,
        String transactionDate,
        String type,
        Category category,
        String approveAmount,
        String balanceAfter,
        String code
) {
}
