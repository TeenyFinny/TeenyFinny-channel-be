package dev.syntax.domain.goal.service;

import dev.syntax.domain.goal.entity.Goal;

public interface GoalAccountService {
	/**
	 * 목표 계좌 생성
	 *
	 * @param goal 목표 계좌 정보
	 */
	Goal createGoalAccount(Goal goal);
}
