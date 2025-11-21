package dev.syntax.domain.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 로그인 응답 DTO.
 *
 * @param accessToken 발급된 JWT 액세스 토큰
 * @param user        로그인한 사용자 정보
 */
@Builder
public record LoginRes(
	String accessToken,
	UserLoginInfo user
) {

	private static final String DEFAULT_TOKEN_TYPE = "Bearer";

	public static LoginRes of(@NotNull UserContext userContext, String accessToken) {
		return LoginRes.builder()
			.accessToken(accessToken)
			.user(
				UserLoginInfo.builder()
					.userId(userContext.getId())
					.role(userContext.getRole())
					.email(userContext.getEmail())
					.build()
			)
			.build();
	}
}
