package dev.syntax.global.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.user.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalBalanceProvider implements BalanceProvider {

	@Override
	public long getUserTotalBalance(User user) {
		return 0L; // Core 연동 전까지 임시 값
	}

	@Override
	public long getUserBalanceByType(User user, AccountType type) {
		return 0L; // Core 연동 전까지 임시 값
	}

	@Override
	public Map<Long, Long> getBalancesForUsers(List<Long> userIds) {
		// 모든 사용자 잔액을 0으로 반환하는 임시 구현
		return userIds.stream()
			.collect(Collectors.toMap(id -> id, id -> 0L));
	}

	@Override
	public Map<AccountType, Long> getBalancesByType(User user) {
		// 모든 타입 잔액을 0으로 반환하는 임시 구현
		return Map.of(
			AccountType.ALLOWANCE, 0L,
			AccountType.GOAL, 0L,
			AccountType.INVEST, 0L
		);
	}
}
