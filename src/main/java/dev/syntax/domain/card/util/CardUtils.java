package dev.syntax.domain.card.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 카드 도메인 관련 유틸리티 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CardUtils {

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
            return number; // 유효하지 않은 경우 원본 반환 (또는 예외 처리)
        }
        return number.replaceAll("(.{4})", "$1 ").trim();
    }
}
