package dev.syntax.domain.auth.dto;

/**
 * 이메일 사용 가능 여부 응답 DTO.
 *
 * @param available 사용 가능하면 true, 아니면 false
 */
public record EmailValidationRes(
	boolean available
) {
}
