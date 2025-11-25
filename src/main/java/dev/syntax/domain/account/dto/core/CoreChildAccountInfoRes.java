package dev.syntax.domain.account.dto.core;

import java.util.List;

/**
 * 자녀 계좌 정보 응답 DTO
 * <p>
 * 자녀의 ID와 해당 자녀의 계좌 목록을 포함합니다.
 * </p>
 *
 * @param userId   자녀 사용자 ID
 * @param accounts 자녀 계좌 목록
 */
public record CoreChildAccountInfoRes(
	Long userId,
	List<CoreAccountItemRes> accounts
) {
}
