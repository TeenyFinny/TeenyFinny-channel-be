package dev.syntax.domain.quiz.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.syntax.domain.quiz.dto.QuizProgressRes;
import dev.syntax.domain.quiz.service.QuizService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 퀴즈 진행도 관련 API 요청을 처리하는 컨트롤러입니다.
 */
@Slf4j
@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * 사용자 기준 퀴즈 진행도를 조회합니다.
     *
     * @param context 인증된 사용자 컨텍스트
     * @return 퀴즈 진행도 정보
     */
    @GetMapping("/progresses")
    public ResponseEntity<BaseResponse<?>> getQuizProgress(@CurrentUser UserContext context) {
        QuizProgressRes response = quizService.getQuizProgress(context);
        log.info("퀴즈 진행도 조회 성공: userId = {}", context.getId());
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
}
