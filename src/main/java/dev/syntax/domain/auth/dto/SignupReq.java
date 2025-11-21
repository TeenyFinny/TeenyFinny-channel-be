package dev.syntax.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupReq(

	@NotBlank @Email
	String email,

	@NotBlank @Size(min = 8, max = 64)
	String password,

	@NotBlank
	String name,

	@NotNull
	String role,

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
