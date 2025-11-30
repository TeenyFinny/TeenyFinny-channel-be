package dev.syntax.domain.account.enums;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum AccountType {
	DEPOSIT,
	ALLOWANCE,
	GOAL,
	
	@JsonAlias("INVESTMENT")  // Core API에서 "INVESTMENT"로 오는 값을 INVEST로 매핑
	INVEST
}
