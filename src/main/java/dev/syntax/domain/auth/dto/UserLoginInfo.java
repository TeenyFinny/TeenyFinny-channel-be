package dev.syntax.domain.auth.dto;

import lombok.Builder;

/**
 * 로그인한 사용자에 대한 최소한의 식별 정보를 담는 DTO
 *
 * @param userId 인증된 사용자의 고유 ID
 * @param role   사용자의 역할(Role), 예: PARENT, CHILD
 * @param email  사용자의 이메일 주소
 */
@Builder
public record UserLoginInfo(
	Long userId,
	String role,
	String email
) {
}
