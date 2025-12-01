package dev.syntax.domain.auth.dto;

import dev.syntax.global.auth.dto.UserContext;
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
	String email,
	Long familyId // 가족 id 존재 여부 확인
) {
	/**
	 * User 엔티티로부터 UserLoginInfo를 생성하는 팩토리 메서드
	 *
	 * @param user User 엔티티
	 * @return UserLoginInfo 인스턴스
	 */
	public static UserLoginInfo of(UserContext user) {
		return UserLoginInfo.builder()
			.userId(user.getId())
			.role(user.getRole())
			.email(user.getEmail())
			.familyId(user.getFamilyId())
			.build();
	}
}
