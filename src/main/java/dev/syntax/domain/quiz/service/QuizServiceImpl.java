package dev.syntax.domain.quiz.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.quiz.dto.QuizProgressRes;
import dev.syntax.domain.quiz.entity.QuizProgress;
import dev.syntax.domain.quiz.repository.QuizProgressRepository;
import dev.syntax.domain.quiz.service.QuizService;
import dev.syntax.global.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizServiceImpl implements QuizService {

    private final QuizProgressRepository quizProgressRepository;

    @Override
    public QuizProgressRes getQuizProgress(UserContext context) {
        Long userId = context.getId();
        QuizProgress progress = quizProgressRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 진행도가 존재하지 않습니다."));

        return QuizProgressRes.builder()
                .progressId(progress.getId())
                .streakDays(progress.getStreakDays())
                .courseCompleted(progress.isCourseCompleted())
                .quizDate(progress.getQuizDate())
                .monthlyReward(progress.isMonthlyReward())
                .todaySolved(progress.getTodaySolved())
                .coupon(progress.getCoupon())
                .requestCompleted(progress.isRequestCompleted())
                .build();
    }
}
