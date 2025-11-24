package dev.syntax.domain.account.dto;

/**
 * @typedef AccountHistoryRes
 *
 * 계좌 "거래내역 한 건"을 나타내는 응답 DTO.
 * 프론트 UI에서 그대로 사용되는 형태로 변환된 데이터이다.
 *
 * 코어 서버에서 내려온 거래내역을 서비스 서버에서 매핑하여,
 * UI에 표시하기 적합한 형태(문자열, 콤마 처리, 날짜 포맷 등)로 가공한 최종 응답이다.
 *
 * @property {String} id
 *   거래 ID. 고유 식별자.
 *
 * @property {String} type
 *   거래 유형.
 *   - "deposit" (입금)
 *   - "withdrawal" (출금)
 *
 * @property {String} merchant
 *   거래처(가맹점) 이름.
 *   예: "스타벅스", "편의점", "이체"
 *
 * @property {String} amount
 *   거래 금액.
 *   천단위 콤마 적용 후 문자열로 전달됨.
 *   예: "5,000"
 *
 * @property {String} balanceAfter
 *   거래 이후 잔액.
 *   천단위 콤마 적용.
 *   예: "95,000"
 *
 * @property {String} timestamp
 *   거래 발생 시각.
 *   'yyyy-MM-dd HH:mm' 포맷의 문자열.
 *   예: "2025-01-15 13:22"
 */
public record AccountHistoryRes(
        String id,
        String type,
        String merchant,
        String amount,
        String balanceAfter,
        String timestamp
) {}
