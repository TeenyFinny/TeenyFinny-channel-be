package dev.syntax.global.service;

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
}
