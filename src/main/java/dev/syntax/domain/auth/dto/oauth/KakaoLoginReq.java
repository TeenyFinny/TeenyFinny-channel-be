package dev.syntax.domain.auth.dto.oauth;

import jakarta.validation.constraints.NotBlank;

/**
 * 카카오 로그인 요청 DTO
 *
 * @param code        카카오 인증 서버로부터 받은 Authorization Code
 * @param redirectUri 카카오 OAuth 리다이렉트 URI (프론트엔드)
 */
public record KakaoLoginReq(
	@NotBlank(message = "인증 코드는 필수입니다.")
	String code,

	@NotBlank(message = "리다이렉트 URI는 필수입니다.")
	String redirectUri
) {
}
