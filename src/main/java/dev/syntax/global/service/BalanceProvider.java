package dev.syntax.global.service;

import java.util.List;
import java.util.Map;

import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.user.entity.User;

/**
 * BalanceProvider
 *
 * <p>사용자의 잔액 정보를 조회하기 위한 추상화 인터페이스입니다.
 * Core 서비스와의 통신 방식을 캡슐화하여, HomeService 등 도메인 서비스가
 * 계좌 잔액 조회 구현에 직접 의존하지 않도록 합니다.</p>
 *
 * <h2>역할</h2>
 * <ul>
 *     <li>한 사용자의 전체 잔액 조회</li>
 *     <li>한 사용자의 계좌 타입별 잔액 조회</li>
 *     <li>부모 사용자의 자녀 여러 명에 대한 잔액을 일괄(batch) 조회</li>
 *     <li>한 자녀의 ALLOWANCE / GOAL / INVEST 잔액을 한 번에 조회</li>
 * </ul>
 *
 * <p>배치 조회 기능(getBalancesForUsers, getBalancesByType)은
 * Core 서비스 연동 시 자녀 수 또는 계좌 타입 수만큼 API가 반복 호출되는
 * N+1 문제를 방지하기 위한 설계입니다.</p>
 *
 * <p>Local 환경에서는 LocalBalanceProvider에서 기본값(0L)을 반환하며,
 * 운영 환경에서는 CoreBalanceProvider에서 실제 Core API 연동 로직을 구현합니다.</p>
 *
 * @see dev.syntax.global.service.LocalBalanceProvider
 * @see dev.syntax.domain.user.entity.User
 * @see dev.syntax.domain.account.enums.AccountType
 */
public interface BalanceProvider {
	long getUserTotalBalance(User user);

	long getUserBalanceByType(User user, AccountType type);

	// 자녀 잔액 배치 조회
	Map<Long, Long> getBalancesForUsers(List<Long> userIds);

	// 특정 사용자 계좌 타입별 잔액 배치 조회
	Map<AccountType, Long> getBalancesByType(User user);
}
