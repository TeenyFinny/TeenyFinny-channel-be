package dev.syntax.domain.quiz.controller;

import dev.syntax.domain.quiz.dto.QuizInfoRes;
import dev.syntax.domain.quiz.dto.QuizProgressRes;
import dev.syntax.domain.quiz.service.QuizService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * QuizController
 *
 * <p>퀴즈 관련 API 요청을 처리하는 컨트롤러입니다.</p>
 *
 * <p>다음 기능을 제공합니다:</p>
 * <ul>
 *     <li>사용자 기준 퀴즈 진행도 조회</li>
 *     <li>특정 퀴즈 정보 조회</li>
 * </ul>
 *
 * <p>인증된 사용자 정보는 {@link UserContext}를 통해 주입받습니다.</p>
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
     * <p>인증된 사용자 정보를 기반으로 DB에서 퀴즈 진행도를 조회하고,
     * {@link BaseResponse} 형태로 반환합니다.</p>
     *
     * @param context 인증된 사용자 컨텍스트
     * @return {@link BaseResponse}에 래핑된 {@link QuizProgressRes} 객체
     */
    @GetMapping("/progresses")
    public ResponseEntity<BaseResponse<?>> getQuizProgress(@CurrentUser UserContext context) {
        QuizProgressRes response = quizService.getQuizProgress(context);
        log.info("퀴즈 진행도 조회 성공: userId = {}", context.getId());
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }

    /**
     * 사용자 기준 퀴즈 진행도를 새로 생성합니다.
     *
     * @param context 인증된 사용자 컨텍스트
     * @return 생성된 퀴즈 진행도 정보
     */
    @PostMapping("/progresses")
    public ResponseEntity<BaseResponse<?>> createQuizProgress(@CurrentUser UserContext context) {
        QuizProgressRes response = quizService.createQuizProgress(context);
        log.info("퀴즈 진행도 생성 성공: userId = {}", context.getId());
        return ApiResponseUtil.success(SuccessCode.CREATED, response);
    }

    /**
     * 특정 퀴즈 정보를 조회합니다.
     *
     * <p>퀴즈 ID를 기반으로 DB에서 퀴즈 정보를 조회하고,
     * {@link BaseResponse} 형태로 반환합니다.</p>
     *
     * @param quizId 조회할 퀴즈의 ID
     * @return {@link BaseResponse}에 래핑된 {@link QuizInfoRes} 객체
     */
    @GetMapping("/info")
    public ResponseEntity<BaseResponse<?>> getQuizInfo(
            @RequestParam("quiz_id") Long quizId
    ) {
        QuizInfoRes response = quizService.getQuizInfo(quizId);
        log.info("퀴즈 정보 조회 성공: quizId = {}", quizId);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }

}
