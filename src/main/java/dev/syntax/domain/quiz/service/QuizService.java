package dev.syntax.domain.quiz.service;

import dev.syntax.domain.quiz.dto.QuizProgressRes;
import dev.syntax.global.auth.dto.UserContext;

/**
 * 퀴즈 진행도 관련 비즈니스 로직 정의
 */
public interface QuizService {

    /**
     * 사용자 기준 퀴즈 진행도를 조회합니다.
     *
     * @param context 인증된 사용자 컨텍스트
     * @return 퀴즈 진행도 정보
     */
    QuizProgressRes getQuizProgress(UserContext context);
}
