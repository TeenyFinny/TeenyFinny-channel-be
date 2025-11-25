package dev.syntax.domain.account.dto.core;

import java.math.BigDecimal;

import dev.syntax.domain.account.enums.AccountType;

public record CoreAccountItemRes(Long accountId,
								 String accountNumber,
								 AccountType accountType,
								 BigDecimal balance) {
}
