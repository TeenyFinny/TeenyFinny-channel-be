package dev.syntax.global.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 공통적으로 사용되는 서비스 로직은 이곳에 작성해주세요.
 */
public class Utils {

	// ThreadLocal을 사용하여 DecimalFormat 인스턴스 캐싱 및 스레드 안정성 확보
	private static final ThreadLocal<DecimalFormat> NUMBER_FORMATTER =
		ThreadLocal.withInitial(() -> new DecimalFormat("#,###"));

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

	/**
	 * BigDecimal 값을 천 단위 구분 기호(,)가 포함된 문자열로 변환합니다.
	 * <p>
	 * 예) 230010000.00 → "230,010,000"
	 * 예) 123.56 → "124" (반올림)
	 * 예) 123.49 → "123" (반올림)
	 * </p>
	 *
	 * <p><b>주의:</b> 이 메소드는 {@code #,###} 패턴을 사용하며, 소수점 이하는
	 * **반올림(Rounding)** 처리되어 정수 부분만 표시됩니다.
	 * 정확한 소수점을 포함하려면 {@code String.format("%,.2f", num.doubleValue())}
	 * 또는 {@code DecimalFormat}에 적절한 패턴을 설정하여 사용하세요.</p>
	 *
	 * @param num 천 단위 구분 기호로 포맷할 BigDecimal 값
	 * @return 천 단위 구분 기호가 포함된 문자열 (소수점 반올림 처리)
	 */
	public static String NumberFormattingService(BigDecimal num) {
		if (num == null) {
			return "0";
		}
		return NUMBER_FORMATTER.get().format(num);
	}
}
