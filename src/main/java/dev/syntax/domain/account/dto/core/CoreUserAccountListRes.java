package dev.syntax.domain.account.dto.core;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 부모 사용자 계좌 목록과 자녀 계좌 정보를 함께 반환하는 Core 응답 DTO.
 * <p>
 * - accounts: 부모 계좌 목록
 * - children: 자녀 정보 + 자녀 계좌 목록 (없으면 응답에서 제외)
 * </p>
 */
public record CoreUserAccountListRes(
	List<CoreAccountItemRes> accounts,
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	List<CoreChildAccountInfoRes> children
) {
}