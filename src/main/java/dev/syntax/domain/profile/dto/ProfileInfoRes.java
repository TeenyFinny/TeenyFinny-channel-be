package dev.syntax.domain.profile.dto;

import lombok.Builder;

/**
 * 프로필 정보 조회 응답 DTO
 * <p>
 * 사용자의 프로필 정보를 클라이언트에 반환하기 위한 응답 객체입니다.
 * </p>
 */
@Builder
public record ProfileInfoRes(
	/**
	 * 사용자 프로필 정보
	 */
	UserProfile user
) {
}
