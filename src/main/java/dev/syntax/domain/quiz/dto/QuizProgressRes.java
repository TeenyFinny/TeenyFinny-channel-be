package dev.syntax.domain.quiz.dto;

import lombok.Builder;

/**
 * 퀴즈 진행도 조회 응답 DTO입니다.
 */
@Builder
public record QuizProgressRes(
        Long progressId,
        int streakDays,
        boolean courseCompleted,
        int quizDate,
        boolean monthlyReward,
        int todaySolved,
        int coupon,
        boolean requestCompleted
) {}
