package dev.syntax.domain.auth.dto;

import lombok.Builder;

/**
 * 자녀 OTP 검증 응답 DTO입니다.
 */
@Builder
public record OtpVerifyRes(
        Long userId,
        Long parentId
) {
}
