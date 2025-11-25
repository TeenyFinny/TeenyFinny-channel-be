package dev.syntax.domain.account.dto;

public record CoreAccountItemRes(Long accountId,
								 String accountNumber,
								 String accountType,
								 String balance) {
}
