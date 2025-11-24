package dev.syntax.domain.quiz.service;

import dev.syntax.domain.quiz.dto.QuizInfoRes;
import dev.syntax.domain.quiz.entity.QuizInfo;
import dev.syntax.domain.quiz.repository.QuizInfoRepository;
import dev.syntax.global.response.error.ErrorBaseCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.global.exception.BusinessException;

import dev.syntax.domain.quiz.dto.QuizProgressRes;
import dev.syntax.domain.quiz.entity.QuizProgress;
import dev.syntax.domain.quiz.repository.QuizProgressRepository;
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
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.QUIZ_PROGRESS_NOT_FOUND));

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

    private final QuizInfoRepository quizInfoRepository;

    @Override
    public QuizInfoRes getQuizInfo(Long quizId, UserContext context) {
        QuizInfo quiz = quizInfoRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.QUIZ_INFO_NOT_FOUND));

        return QuizInfoRes.builder()
                .title(quiz.getTitle())
                .info(quiz.getInfo())
                .question(quiz.getQuestion())
                .answer(quiz.getAnswer())
                .explanation(quiz.getExplanation())
                .build();
    }
}
