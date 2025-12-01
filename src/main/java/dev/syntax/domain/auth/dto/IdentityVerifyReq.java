package dev.syntax.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 본인인증 요청 DTO
 */
public record IdentityVerifyReq(
        @NotBlank String carrier,           // 통신사
        @NotBlank @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "전화번호 형식이 올바르지 않습니다.") String phoneNumber, // 휴대폰 번호
        @NotBlank @Pattern(regexp = "\\d{6}", message = "생년월일 앞 6자리") String birthFront,
        @NotBlank @Pattern(regexp = "\\d{1}", message = "생년월일 뒤 1자리") String birthBack,
        @NotBlank String name               // 이름
) {}
