package dev.syntax.domain.auth.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 토큰 응답 DTO (내부용)
 * 카카오 인증 서버로부터 받는 토큰 정보
 *
 * @param accessToken  카카오 액세스 토큰
 * @param tokenType    토큰 타입 (bearer)
 * @param expiresIn    만료 시간 (초)
 * @param refreshToken 리프레시 토큰
 */
public record KakaoTokenRes(
	@JsonProperty("access_token")
	String accessToken,

	@JsonProperty("token_type")
	String tokenType,

	@JsonProperty("expires_in")
	Integer expiresIn,

	@JsonProperty("refresh_token")
	String refreshToken,

	@JsonProperty("refresh_token_expires_in")
	Integer refreshTokenExpiresIn
) {
}
