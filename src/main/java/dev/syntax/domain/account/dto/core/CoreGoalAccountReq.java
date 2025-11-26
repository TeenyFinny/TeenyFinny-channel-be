package dev.syntax.domain.account.dto.core;

import lombok.Builder;

@Builder
public record CoreGoalAccountReq(
	Long childCoreId,
	String name) {
}