package dev.syntax.domain.goal.service;

public interface GoalAccountService {
    /**
     * 목표 계좌 생성
     *
     * @param userId   사용자 ID
     * @param goalName 목표 이름
     */
    void createGoalAccount(Long userId, String goalName);
}
