package dev.syntax.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 로그인 요청 DTO.
 *
 * @param email    사용자 이메일
 * @param password 비밀번호(8~64자)
 */
public record LoginReq(@NotBlank @Email String email,
					   @NotBlank @Size(min = 8, max = 64) String password) {
}
