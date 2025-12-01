package dev.syntax.domain.account.dto;

/**
 * 계좌 거래내역 한 건을 나타내는 응답 DTO (Channel 서버 → 프론트엔드)
 * <p>
 * 코어 서버에서 내려온 거래내역을 서비스 서버에서 매핑하여,
 * UI에 표시하기 적합한 형태(문자열, 콤마 처리, 날짜 포맷 등)로 가공한 최종 응답입니다.
 * </p>
 *
 * @param transactionId 거래 ID (고유 식별자)
 * @param code 거래 코드 (WITHDRAW, DEPOSIT 등)
 * @param merchant 거래처(가맹점) 이름 (예: "스타벅스", "편의점", "이체")
 * @param amount 거래 금액 (천단위 콤마 적용, 예: "5,000")
 * @param balanceAfter 거래 이후 잔액 (천단위 콤마 적용, 예: "95,000")
 * @param category 카테고리 한글명 (예: "식비", "교통")
 * @param timestamp 거래 발생 시각 (포맷: "yyyy-MM-dd HH:mm", 예: "2025-01-15 13:22")
 */
public record AccountHistoryRes(
        Long transactionId,
        String code,
        String merchant,
        String amount,
        String balanceAfter,
        String category,
        String timestamp
) {}
