package dev.syntax.global.service;

import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.user.entity.User;

public interface BalanceProvider {
	long getUserTotalBalance(User user);

	long getUserBalanceByType(User user, AccountType type);
}
