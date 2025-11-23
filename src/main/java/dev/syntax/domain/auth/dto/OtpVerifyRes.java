package dev.syntax.domain.auth.dto;

import lombok.Builder;

/**
 * 자녀 OTP 검증 응답 DTO입니다.
 * 가족 등록 완료 후 새로운 JWT 토큰도 함께 반환합니다.
 */
@Builder
public record OtpVerifyRes(
        Long userId,
        Long parentId,
        String accessToken  // 새로운 JWT 토큰
) {
}
