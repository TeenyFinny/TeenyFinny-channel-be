package dev.syntax.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 이메일 사용 가능 여부 요청 DTO.
 *
 * @param email 검증할 이메일
 */
public record EmailValidationReq(
	@NotBlank @Email String email
) {
}
