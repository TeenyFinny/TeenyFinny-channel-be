package dev.syntax.domain.account.enums;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum AccountType {
	DEPOSIT,
	ALLOWANCE,
	GOAL,
    @JsonAlias("INVESTMENT")
	INVEST
}
