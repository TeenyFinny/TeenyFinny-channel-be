package dev.syntax.domain.auth.dto.oauth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 카카오 신규 회원가입 요청 DTO
 *
 * @param tempToken      임시 토큰
 * @param role           사용자 역할 (PARENT/CHILD)
 * @param name           이름
 * @param birthDate      생년월일 (YYYY-MM-DD)
 * @param gender         성별 (1: 남, 2: 여)
 * @param phoneNumber    전화번호
 * @param simplePassword 간편 비밀번호 (6자리 숫자)
 */
public record KakaoSignupReq(
	@NotBlank(message = "임시 토큰은 필수입니다.")
	String tempToken,

	@NotBlank(message = "역할은 필수입니다.")
	@Pattern(regexp = "PARENT|CHILD", message = "역할은 PARENT 또는 CHILD여야 합니다.")
	String role,

	@NotBlank(message = "이름은 필수입니다.")
	String name,

	@NotBlank(message = "생년월일은 필수입니다.")
	@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "생년월일 형식이 올바르지 않습니다. (YYYY-MM-DD)")
	String birthDate,

	@NotNull(message = "성별은 필수입니다.")
	Byte gender,

	@NotBlank(message = "전화번호는 필수입니다.")
	String phoneNumber,

	@NotBlank(message = "간편 비밀번호는 필수입니다.")
	@Pattern(regexp = "\\d{6}", message = "간편 비밀번호는 6자리 숫자여야 합니다.")
	String simplePassword
) {
	public KakaoSignupReq {
		// birthDate가 8자리이면 변환
		if (birthDate != null && birthDate.matches("\\d{8}")) {
			birthDate = birthDate.substring(0, 4) + "-" +
				birthDate.substring(4, 6) + "-" +
				birthDate.substring(6, 8);
		}
	}
}