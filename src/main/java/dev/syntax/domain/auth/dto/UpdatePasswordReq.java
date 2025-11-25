package dev.syntax.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 비밀번호 변경 요청 DTO
 * <p>
 * 현재 비밀번호와 새 비밀번호를 받아 비밀번호를 변경합니다.
 * </p>
 */
public record UpdatePasswordReq(
	/**
	 * 현재 비밀번호
	 */
	@NotBlank(message = "현재 비밀번호를 입력해주세요.")
	@Pattern(
		regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
		message = "비밀번호는 8자리 이상이어야 하며, 특수문자를 포함해야 합니다."
	)
	String currentPassword,

	/**
	 * 새 비밀번호 (8자리 이상, 특수문자 포함)
	 */
	@NotBlank(message = "새 비밀번호를 입력해주세요.")
	@Pattern(
		regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
		message = "비밀번호는 8자리 이상이어야 하며, 특수문자를 포함해야 합니다."
	)
	String newPassword
) {
}
