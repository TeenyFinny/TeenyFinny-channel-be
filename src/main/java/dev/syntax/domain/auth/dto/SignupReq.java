package dev.syntax.domain.auth.dto;

import dev.syntax.domain.user.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO.
 *
 * @param email 이메일
 * @param password 비밀번호
 * @param name 사용자 이름
 * @param role 사용자 역할
 * @param simplePassword 간편 비밀번호(6자리)
 * @param birthDate 생년월일(yyyyMMdd)
 * @param gender 성별(1: 남, 2: 여)
 * @param phoneNumber 전화번호
 */
public record SignupReq(

	@NotBlank @Email
	String email,

	@NotBlank @Size(min = 8, max = 64)
	String password,

	@NotBlank
	String name,

	@NotNull
	Role role,

	@Size(min = 6, max = 6)
	String simplePassword,

	@NotNull
	@Pattern(regexp = "\\d{8}", message = "생년월일을 확인해주세요.")
	String birthDate,

	@NotNull
	@Min(1)
	@Max(2)
	Integer gender,

	@NotBlank
	@Pattern(regexp = "\\d{10,11}", message = "전화번호는 숫자 10~11자리여야 합니다.")
	String phoneNumber

) {
}
