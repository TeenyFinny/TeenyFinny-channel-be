package dev.syntax.domain.auth.dto;

import lombok.Builder;

/**
 * 토큰 갱신 응답 DTO입니다.
 */
@Builder
public record RefreshTokenRes(
        String accessToken
) {
}
