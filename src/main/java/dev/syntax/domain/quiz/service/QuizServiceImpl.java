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

    @Override
    public QuizProgressRes createQuizProgress(UserContext context) {
        Long userId = context.getId();

        quizProgressRepository.findByUserId(userId).ifPresent(p -> {
            throw new BusinessException(ErrorBaseCode.CONFLICT);
        });

        QuizProgress progress = QuizProgress.builder()
                .userId(userId)
                .streakDays(0)
                .courseCompleted(false)
                .quizDate(0)
                .monthlyReward(false)
                .todaySolved(0)
                .coupon(0)
                .requestCompleted(false)
                .build();

        QuizProgress saved = quizProgressRepository.save(progress);
        return QuizProgressRes.builder()
                .progressId(saved.getId())
                .streakDays(saved.getStreakDays())
                .courseCompleted(saved.isCourseCompleted())
                .quizDate(saved.getQuizDate())
                .monthlyReward(saved.isMonthlyReward())
                .todaySolved(saved.getTodaySolved())
                .coupon(saved.getCoupon())
                .requestCompleted(saved.isRequestCompleted())
                .build();
    }


    private final QuizInfoRepository quizInfoRepository;

    @Override
    public QuizInfoRes getQuizInfo(Long quizId) {
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
