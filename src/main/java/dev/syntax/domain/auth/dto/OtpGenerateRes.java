package dev.syntax.domain.auth.dto;

import lombok.Builder;

/**
 * 부모 OTP 생성 응답 DTO입니다.
 */
@Builder
public record OtpGenerateRes(
        String familyOtp
) {
}
