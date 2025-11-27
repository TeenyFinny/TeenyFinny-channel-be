package dev.syntax.domain.auth.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 사용자 정보 응답 DTO (내부용)
 * 카카오 API로부터 받는 사용자 정보
 */
public record KakaoUserInfo(
	Long id,

	@JsonProperty("connected_at")
	String connectedAt,

	@JsonProperty("kakao_account")
	KakaoAccount kakaoAccount
) {
	public record KakaoAccount(
		@JsonProperty("profile_nickname_needs_agreement")
		Boolean profileNicknameNeedsAgreement,

		@JsonProperty("profile")
		Profile profile,

		@JsonProperty("has_email")
		Boolean hasEmail,

		@JsonProperty("email_needs_agreement")
		Boolean emailNeedsAgreement,

		@JsonProperty("is_email_valid")
		Boolean isEmailValid,

		@JsonProperty("is_email_verified")
		Boolean isEmailVerified,

		@JsonProperty("email")
		String email
	) {
	}

	public record Profile(
		@JsonProperty("nickname")
		String nickname,

		@JsonProperty("is_default_nickname")
		Boolean isDefaultNickname
	) {
	}

	/**
	 * 카카오 사용자 ID를 providerId 형식으로 변환
	 */
	public String getProviderId() {
		return "kakao_" + id;
	}

	/**
	 * 이메일 추출
	 */
	public String getEmail() {
		return kakaoAccount != null ? kakaoAccount.email : null;
	}

	/**
	 * 닉네임 추출
	 */
	public String getNickname() {
		return kakaoAccount != null && kakaoAccount.profile != null
			? kakaoAccount.profile.nickname
			: null;
	}
}
