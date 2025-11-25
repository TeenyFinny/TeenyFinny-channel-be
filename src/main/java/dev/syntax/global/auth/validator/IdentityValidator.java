package dev.syntax.global.auth.validator;


import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class IdentityValidator {

    private IdentityValidator() {
        throw new UnsupportedOperationException("Utility class");
    }


    private static final DateTimeFormatter BIRTH_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd").withResolverStyle(ResolverStyle.STRICT);;

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


        // 1) 앞자리(YYMMDD) 존재 & 형식 검증
        if (!StringUtils.hasText(birthFront)) {
            throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
        }

        LocalDate datePart;
        try {
            datePart = LocalDate.parse(birthFront, BIRTH_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
        }

        // 2) 뒷자리(1~4) 검증
        if (!("1".equals(birthBack) || "2".equals(birthBack)
                || "3".equals(birthBack) || "4".equals(birthBack))) {
            throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
        }

        // 3) 세기 계산 및 앞자리와의 일관성 검증
        int century = ("1".equals(birthBack) || "2".equals(birthBack)) ? 1900 : 2000;
        int fullYear = century + (datePart.getYear() % 100);

        // (선택) 미래 날짜 방지
        LocalDate fullBirth = LocalDate.of(fullYear,
                datePart.getMonthValue(),
                datePart.getDayOfMonth());

        if (fullBirth.isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
        }

        // 유효하면 아무것도 하지 않음 = 통과
    }

    /** 전화번호 숫자만, 길이 10~11 */
    public static void validatePhone(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber) || !phoneNumber.matches("\\d{10,11}")) {
            throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
        }
    }
}