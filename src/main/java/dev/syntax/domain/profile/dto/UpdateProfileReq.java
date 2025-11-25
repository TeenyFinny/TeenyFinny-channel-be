package dev.syntax.domain.profile.dto;

import jakarta.validation.constraints.Pattern;

/**
 * 프로필 수정 요청 DTO
 * <p>
 * 사용자가 수정 가능한 프로필 정보를 담는 객체입니다.
 * 모든 필드는 선택적(Optional)이며, 제공된 필드만 업데이트됩니다.
 * </p>
 */
public record UpdateProfileReq(

	String name,

	/**
	 * 수정할 전화번호 (선택)
	 * <p>
	 * 형식: 010-1234-5678 또는 01012345678
	 * 제공되지 않으면 기존 값 유지
	 * </p>
	 */
	@Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다")
	String phoneNumber
) {
}
