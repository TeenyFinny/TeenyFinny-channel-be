package dev.syntax.domain.account.dto;

/**
 * 계좌 거래내역 상세 조회 응답 DTO (Channel 서버 → 프론트엔드)
 * <p>
 * 단일 거래의 상세 정보를 포함합니다.
 * 모든 금액은 천단위 콤마가 적용된 문자열 형태입니다.
 * </p>
 *
 * @param merchant 거래처명
 * @param amount 거래금액 (콤마 포함, 예: "5,000")
 * @param date 거래일시 (포맷: "yyyy.MM.dd HH:mm:ss")
 * @param type 결제 방식 ("일시불", "할부", 또는 빈 문자열)
 * @param category 카테고리 한글명 (예: "식비", "교통")
 * @param approveAmount 승인금액 (콤마 포함, 예: "5,000")
 * @param balanceAfter 거래 후 잔액 (콤마 포함, 예: "150,300")
 * @param code 거래 코드 (WITHDRAW, DEPOSIT 등)
 */
public record AccountHistoryDetailRes(
        String merchant,
        String amount,
        String date,
        String type,
        String category,
        String approveAmount,
        String balanceAfter,
        String code
) {
}
