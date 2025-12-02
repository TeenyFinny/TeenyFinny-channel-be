package dev.syntax.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 비밀번호 재설정 요청 DTO
 */
public record ResetPasswordReq(
	@NotBlank @Email(message = "이메일 형식이 올바르지 않습니다.") String email,
	@NotBlank @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "전화번호 형식이 올바르지 않습니다.") String phoneNumber,
	@NotBlank @Pattern(regexp = "\\d{8}", message = "생년월일 형식이 올바르지 않습니다.") String birthDate, // YYYYMMDD
	@NotBlank String name
) {}

