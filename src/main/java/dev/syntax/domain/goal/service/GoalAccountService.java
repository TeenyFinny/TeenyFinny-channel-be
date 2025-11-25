package dev.syntax.domain.goal.service;

import dev.syntax.domain.goal.dto.GoalAccountCreateRes;

public interface GoalAccountService {
    /**
     * 목표 계좌 생성
     *
     * @param userId   사용자 ID
     * @param goalName 목표 이름
     */
    GoalAccountCreateRes createGoalAccount(Long userId, String goalName);
}
