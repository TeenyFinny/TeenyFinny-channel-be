package dev.syntax.domain.account.dto.core;

import java.math.BigDecimal;

public record CoreAccountItemRes(Long accountId,
								 String accountNumber,
								 String accountType,
								 BigDecimal balance) {
}
