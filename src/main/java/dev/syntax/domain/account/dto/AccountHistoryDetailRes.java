package dev.syntax.domain.account.dto;

/**
 * 계좌 거래내역 상세 조회 응답 DTO.
 *
 * @param merchant      거래처명
 * @param amount        거래금액 (콤마 포함)
 * @param date          거래일시 (yyyy-MM-dd HH:mm)
 * @param type          거래유형 (출금/입금 등)
 * @param category      카테고리 (식비, 교통 등)
 * @param approveAmount 승인금액 (콤마 포함)
 * @param balanceAfter  거래 후 잔액 (콤마 포함)
 * @param code          거래 코드(일시불/할부))
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
