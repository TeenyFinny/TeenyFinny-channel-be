package dev.syntax.global.service;

/**
 * 공통적으로 사용되는 서비스 로직은 이곳에 작성해주세요.
 */
public class Utils {

    /**
     * 정수 값을 천 단위 구분 기호(,)가 포함된 문자열로 변환합니다.
     * <p>
     * 예) 230010000 → "230,010,000"
     * </p>
     *
     * <p><b>주의:</b> {@link String#format(String, Object...)}는 기본 Locale의
     * 숫자 형식 규칙을 따릅니다. 일관된 결과가 필요하면
     * {@code String.format(Locale.US, "%,d", num)}처럼 Locale을 명시하세요.</p>
     *
     * @param num 천 단위 구분 기호로 포맷할 정수 값
     * @return 천 단위 구분 기호가 포함된 문자열
     */
    public static String NumberFormattingService(int num) {
        return String.format("%,d", num);
    }
}
