package dev.syntax.domain.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 OAuth 임시 토큰 엔티티
 * 
 * 신규 카카오 사용자의 임시 인증 토큰을 저장합니다.
 * Redis 대신 DB를 사용하여 토큰을 관리합니다.
 */
@Entity
@Table(name = "kakao_temp_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoTempToken {

	@Id
	@Column(length = 64)
	private String token;

	@Column(nullable = false, length = 100)
	private String providerId;

	@Column(length = 100)
	private String kakaoEmail;

	@Column(length = 50)
	private String kakaoName;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Builder
	public KakaoTempToken(
		String token,
		String providerId,
		String kakaoEmail,
		String kakaoName,
		LocalDateTime expiresAt
	) {
		this.token = token;
		this.providerId = providerId;
		this.kakaoEmail = kakaoEmail;
		this.kakaoName = kakaoName;
		this.expiresAt = expiresAt;
		this.createdAt = LocalDateTime.now();
	}

	/**
	 * 토큰이 만료되었는지 확인
	 */
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiresAt);
	}
}
