package dev.syntax.domain.home.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import dev.syntax.domain.user.enums.Role;
import lombok.Builder;

/**
 * 홈 화면 API 응답 DTO입니다.
 */
@Builder
public record HomeRes(
	UserDto user
) {

	/**
	 * 사용자 정보 및 역할별 추가 데이터를 포함하는 DTO입니다.
	 */
	@Builder
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record UserDto(
		Long userId,
		String name,
		Role role,
		String email,
		String balance, // 부모
		List<ChildDto> children, // 부모

		String totalBalance, // 자녀
		String depositBalance, // 자녀
		String investmentBalance, // 자녀
		String savingBalance // 자녀
	) {
	}

	/**
	 * 부모가 조회할 때 포함되는 자녀 정보 DTO입니다.
	 */
	@Builder
	public record ChildDto(
		Long userId,
		String name,
		String balance,
		Byte gender
	) {
	}
}
