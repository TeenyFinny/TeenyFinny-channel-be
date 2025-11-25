package dev.syntax.domain.card.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 카드 도메인 관련 유틸리티 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CardUtils {

    /**
     * 카드 번호를 4자리씩 띄어쓰기로 포맷팅합니다.
     * <p>
     * 예: "1234567812345678" -> "1234 5678 1234 5678"
     * </p>
     *
     * @param number 원본 카드 번호 (16자리 숫자 문자열)
     * @return 포맷팅된 카드 번호
     */
    public static String formatCardNumber(String number) {
        if (number == null || number.length() != 16) {
            throw new IllegalArgumentException("카드 번호는 16자리 숫자 문자열이어야 합니다.");
        }
        return number.replaceAll("(.{4})", "$1 ").trim();
    }

    /**
     * 카드 만료일을 DB 저장 형식(yyMM)에서 프론트 표시 형식(MM/yy)으로 변환합니다.
     * <p>
     * 예: "2512" -> "12/25"
     * </p>
     *
     * @param yyMM 원본 만료일 문자열 (yyMM 형식, 4자리)
     * @return MM/yy 형식으로 변환된 만료일
     */
    public static String formatExpiredAt(String yyMM) {
        if (yyMM == null || yyMM.length() != 4) return yyMM;
        String yy = yyMM.substring(0, 2);
        String mm = yyMM.substring(2, 4);
        return mm + "/" + yy;
    }
}
