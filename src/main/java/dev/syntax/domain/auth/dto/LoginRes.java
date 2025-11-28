package dev.syntax.domain.auth.dto;

import dev.syntax.global.auth.dto.UserContext;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 로그인 응답 DTO.
 *
 * @param tokenType   토큰 종류
 * @param accessToken 발급된 JWT 액세스 토큰
 * @param user        로그인한 사용자 정보
 */
@Builder
public record LoginRes(
	String tokenType,
	String accessToken,
	UserLoginInfo user
) {

	private static final String DEFAULT_TOKEN_TYPE = "Bearer";

	public static LoginRes of(@NotNull UserContext userContext, String accessToken) {
		return LoginRes.builder()
			.tokenType(DEFAULT_TOKEN_TYPE)
			.accessToken(accessToken)
			.user(UserLoginInfo.of(userContext))
			.build();
	}
}
