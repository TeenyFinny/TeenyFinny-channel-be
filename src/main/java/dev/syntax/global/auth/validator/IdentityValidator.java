package dev.syntax.global.auth.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

import org.springframework.util.StringUtils;

import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;

public class IdentityValidator {

	private IdentityValidator() {
		throw new UnsupportedOperationException("Utility class");
	}

	private static final DateTimeFormatter BIRTH_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd")
		.withResolverStyle(ResolverStyle.STRICT);

	// 지원 통신사
	private static final String[] CARRIERS = {"SKT", "KT", "LGU+"};

	/** 통신사 검증 */
	public static void validateCarrier(String carrier) {
		if (!StringUtils.hasText(carrier)) {
			throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
		}

		boolean matched = false;
		for (String c : CARRIERS) {
			if (c.equalsIgnoreCase(carrier)) {
				matched = true;
				break;
			}
		}

		if (!matched) {
			throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
		}
	}

	/**
	 * 주민번호 앞/뒷자리 일관성 검증
	 * birthFront: YYMMDD
	 * birthBack: 1~4 (성별+세기)
	 * 단순 검증용 → 유효하지 않으면 예외 던짐
	 */
	public static void validateBirth(String birthFront, String birthBack) {

		// 1) null / empty check
		if (!StringUtils.hasText(birthFront) || !StringUtils.hasText(birthBack)) {
			throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
		}

		// 2) 뒷자리 1~4인지 확인
		if (!birthBack.matches("[1-4]")) {
			throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
		}

		// 3) 세기 계산
		int century = (birthBack.equals("1") || birthBack.equals("2")) ? 1900 : 2000;

		// 4) 앞자리 YYMMDD → YYYYMMDD
		int year = Integer.parseInt(birthFront.substring(0, 2));
		int month = Integer.parseInt(birthFront.substring(2, 4));
		int day = Integer.parseInt(birthFront.substring(4, 6));

		LocalDate fullBirth;
		try {
			fullBirth = LocalDate.of(century + year, month, day);
		} catch (Exception e) {
			throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
		}

		// 5) 미래 날짜 방지
		if (fullBirth.isAfter(LocalDate.now())) {
			throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
		}
	}

	/** 전화번호 숫자만, 길이 10~11 */
	public static void validatePhone(String phoneNumber) {
		if (!StringUtils.hasText(phoneNumber) || !phoneNumber.matches("\\d{10,11}")) {
			throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
		}
	}
}