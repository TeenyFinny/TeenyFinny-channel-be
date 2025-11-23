package dev.syntax.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 자녀 OTP 검증 요청 DTO입니다.
 */
public record OtpVerifyReq(
        @NotBlank(message = "OTP는 필수입니다.")
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP는 6자리 숫자여야 합니다.")
        String familyOtp
) {
}
