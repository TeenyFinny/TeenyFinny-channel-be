package dev.syntax.domain.auth.dto.oauth;

import dev.syntax.domain.auth.dto.UserLoginInfo;
import lombok.Builder;

/**
 * 카카오 로그인 응답 DTO
 *
 * @param isNewUser   신규 사용자 여부
 * @param user        기존 사용자 정보 (기존 사용자인 경우)
 * @param tokenType   토큰 타입 (기존 사용자인 경우)
 * @param accessToken JWT 액세스 토큰 (기존 사용자인 경우)
 * @param tempToken   임시 토큰 (신규 사용자인 경우)
 * @param kakaoEmail  카카오 이메일 (신규 사용자인 경우)
 * @param kakaoName   카카오 닉네임 (신규 사용자인 경우)
 */
@Builder
public record KakaoLoginRes(
	Boolean isNewUser,
	UserLoginInfo user,
	String tokenType,
	String accessToken,
	String tempToken,
	String kakaoEmail,
	String kakaoName
) {
	/**
	 * 기존 사용자 응답 생성
	 */
	public static KakaoLoginRes forExistingUser(
		UserLoginInfo user,
		String tokenType,
		String accessToken
	) {
		return KakaoLoginRes.builder()
			.isNewUser(false)
			.user(user)
			.tokenType(tokenType)
			.accessToken(accessToken)
			.build();
	}

	/**
	 * 신규 사용자 응답 생성
	 */
	public static KakaoLoginRes forNewUser(
		String tempToken,
		String kakaoEmail,
		String kakaoName
	) {
		return KakaoLoginRes.builder()
			.isNewUser(true)
			.tempToken(tempToken)
			.kakaoEmail(kakaoEmail)
			.kakaoName(kakaoName)
			.build();
	}
}
