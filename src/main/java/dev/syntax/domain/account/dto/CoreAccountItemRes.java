package dev.syntax.domain.account.dto;

import dev.syntax.domain.account.enums.AccountType;

public record CoreAccountItemRes(Long accountId,
                                 String accountNumber,
                                 AccountType accountType,
                                 String balance) {
}
