package dev.syntax.domain.quiz.service;

import dev.syntax.domain.quiz.dto.QuizInfoRes;
import dev.syntax.domain.quiz.dto.QuizProgressUpdateReq;
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


    /**
     * 인증된 사용자 기준으로 퀴즈 진행도를 조회합니다.
     *
     * @param context 인증된 사용자 컨텍스트
     * @return 조회된 퀴즈 진행도를 담은 {@link QuizProgressRes} DTO
     * @throws BusinessException 진행도가 존재하지 않는 경우 {@link ErrorBaseCode#QUIZ_PROGRESS_NOT_FOUND} 발생
     */
    @Override
    public QuizProgressRes getQuizProgress(UserContext context) {
        Long userId = context.getId();
        QuizProgress progress = quizProgressRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.QUIZ_PROGRESS_NOT_FOUND));

        return toDto(progress);
    }


    /**
     * 인증된 사용자 기준으로 새로운 퀴즈 진행도를 생성합니다.
     * <p>
     * 이미 진행도가 존재하면 {@link ErrorBaseCode#CONFLICT} 예외를 발생시킵니다.
     * </p>
     *
     * @param context 인증된 사용자 컨텍스트
     * @return 생성된 퀴즈 진행도를 담은 {@link QuizProgressRes} DTO
     * @throws BusinessException 진행도가 이미 존재하는 경우 {@link ErrorBaseCode#CONFLICT} 발생
     */
    @Transactional
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
                .firstQuizIdToday(0)
                .build();

        QuizProgress saved = quizProgressRepository.save(progress);
        return toDto(saved);
    }


    /**
     * 인증된 사용자 기준으로 퀴즈 진행도를 일부 필드만 업데이트합니다.
     * <p>
     * 요청에서 null이 아닌 필드만 업데이트되며, 존재하지 않는 경우 {@link ErrorBaseCode#QUIZ_PROGRESS_NOT_FOUND} 예외 발생
     * </p>
     *
     * @param context 인증된 사용자 컨텍스트
     * @param req     업데이트할 필드를 담은 {@link QuizProgressUpdateReq} DTO
     * @return 업데이트된 퀴즈 진행도를 담은 {@link QuizProgressRes} DTO
     * @throws BusinessException 진행도가 존재하지 않는 경우 {@link ErrorBaseCode#QUIZ_PROGRESS_NOT_FOUND} 발생
     */
    @Transactional
    @Override
    public QuizProgressRes updateQuizProgress(UserContext context, QuizProgressUpdateReq req) {

        Long userId = context.getId();

        QuizProgress progress = quizProgressRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.QUIZ_PROGRESS_NOT_FOUND));

        // 요청받은 필드만 업데이트 (null 이 아닌 경우)
        progress.update(req);
        QuizProgress saved = quizProgressRepository.save(progress);

        // 응답: 수정된 값만 보여주기 위해 DTO를 그대로 사용
        return toDto(saved);
    }


    private final QuizInfoRepository quizInfoRepository;

    /**
     * 특정 퀴즈 ID에 대한 퀴즈 정보를 조회합니다.
     *
     * @param quizId 조회할 퀴즈 ID
     * @return 퀴즈 정보를 담은 {@link QuizInfoRes} DTO
     * @throws BusinessException 존재하지 않는 퀴즈 ID인 경우 {@link ErrorBaseCode#QUIZ_INFO_NOT_FOUND} 발생
     */
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


    private QuizProgressRes toDto(QuizProgress progress) {
        return QuizProgressRes.builder()
                .progressId(progress.getId())
                .streakDays(progress.getStreakDays())
                .courseCompleted(progress.isCourseCompleted())
                .quizDate(progress.getQuizDate())
                .monthlyReward(progress.isMonthlyReward())
                .todaySolved(progress.getTodaySolved())
                .coupon(progress.getCoupon())
                .requestCompleted(progress.isRequestCompleted())
                .firstQuizIdToday(progress.getFirstQuizIdToday())
                .build();
    }
}
