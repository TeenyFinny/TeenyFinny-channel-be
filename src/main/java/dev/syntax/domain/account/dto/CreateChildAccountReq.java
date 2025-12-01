package dev.syntax.domain.account.dto;

/**
 * 자녀 계좌 생성 요청 DTO
 */
public record CreateChildAccountReq(
		Long childId, // 자녀 ID 추가
		String childName,
		String childPhone,
		String birth,
		String address,
		String detailAddress
) {
}
