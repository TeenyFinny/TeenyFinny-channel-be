package dev.syntax.domain.quiz.controller;

import dev.syntax.domain.quiz.dto.QuizInfoRes;
import dev.syntax.domain.quiz.dto.QuizProgressRes;
import dev.syntax.domain.quiz.dto.QuizProgressUpdateReq;
import dev.syntax.domain.quiz.dto.RequestCompletedRes;
import dev.syntax.domain.quiz.service.QuizService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import dev.syntax.global.response.error.ErrorBaseCode;
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
 *     <li>사용자 기준 퀴즈 진행도 생성</li>
 *     <li>사용자 기준 퀴즈 진행도 업데이트</li>
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
     * @param context 인증된 사용자 컨텍스트
     * @return {@link BaseResponse}에 래핑된 {@link QuizProgressRes} 객체
     */
    @GetMapping("/progresses")
    public ResponseEntity<BaseResponse<?>> getQuizProgress(@CurrentUser UserContext context) {
        QuizProgressRes response = quizService.getQuizProgress(context);
        log.info("퀴즈 진행도 조회 성공: userId={}", context.getId());
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }

    /**
     * 사용자 기준 퀴즈 진행도를 새로 생성합니다.
     *
     * @param context 인증된 사용자 컨텍스트
     * @return 생성된 {@link QuizProgressRes} 객체를 래핑한 {@link BaseResponse}
     */
    @PostMapping("/progresses")
    public ResponseEntity<BaseResponse<?>> createQuizProgress(@CurrentUser UserContext context) {
        QuizProgressRes response = quizService.createQuizProgress(context);
        log.info("퀴즈 진행도 생성 성공: userId={}", context.getId());
        return ApiResponseUtil.success(SuccessCode.CREATED, response);
    }

    /**
     * 사용자 기준 퀴즈 진행도를 업데이트합니다.
     *
     * <p>요청에서 null이 아닌 필드만 업데이트되며, 인증된 사용자 정보를 기준으로
     * 퀴즈 진행도를 조회 후 수정합니다.</p>
     *
     * @param request 업데이트할 필드를 담은 {@link QuizProgressUpdateReq} 객체
     * @param context 인증된 사용자 컨텍스트
     * @return 수정된 {@link QuizProgressRes} 객체를 래핑한 {@link BaseResponse}
     */
    @PatchMapping("/progresses")
    public ResponseEntity<BaseResponse<?>> updateQuizProgress(
            @RequestBody QuizProgressUpdateReq request,
            @CurrentUser UserContext context
    ) {
        QuizProgressRes response = quizService.updateQuizProgress(context, request);
        log.info("퀴즈 진행도 업데이트 성공: userId={}", context.getId());
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }

    /**
     * 특정 퀴즈 정보를 조회합니다.
     *
     * @param quizId 조회할 퀴즈 ID
     * @return {@link QuizInfoRes} 객체를 래핑한 {@link BaseResponse}
     */
    @GetMapping("/info")
    public ResponseEntity<BaseResponse<?>> getQuizInfo(
            @RequestParam("quiz_id") Long quizId
    ) {
        QuizInfoRes response = quizService.getQuizInfo(quizId);
        log.info("퀴즈 정보 조회 성공: quizId={}", quizId);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }

    /**
     * 특정 자녀의 request_completed 여부를 조회합니다.
     *
     * @param context 인증된 사용자 컨텍스트
     * @param childId 자녀 ID
     * @return RequestCompletedRes(Boolean)
     */
    @GetMapping("/{childId}/progresses")
    public ResponseEntity<BaseResponse<?>> getChildQuizProgress(
            @CurrentUser UserContext context,
            @PathVariable Long childId
    ) {

        // 1. 자녀 권한 검증
//        if (!context.getChildIds().contains(childId)) {
//            log.warn("권한 없는 자녀 접근 시도: parentId={}, childId={}", context.getId(), childId);
//            throw new BusinessException(ErrorBaseCode.INVALID_CHILD);
//        }
    
        // 2. 해당 자녀의 request_completed 상태 조회
        boolean requestCompleted = quizService.isRequestCompleted(childId);

        // 3. 응답 생성
        RequestCompletedRes response = new RequestCompletedRes(requestCompleted);
        log.info("자녀 퀴즈 완료 여부 조회: parentId={}, childId={}, completed={}",
                context.getId(), childId, requestCompleted);

        return ApiResponseUtil.success(SuccessCode.OK, response);
    }

}
