package dev.syntax.domain.account.controller;

import dev.syntax.domain.account.dto.CreateChildAccountReq;
import dev.syntax.domain.account.service.BankAccountService;
import dev.syntax.domain.card.dto.CardCreateReq;
import dev.syntax.domain.card.dto.CardInfoRes;
import dev.syntax.domain.card.service.CardCreateService;
import dev.syntax.domain.feedback.dto.FeedbackCreateReq;
import dev.syntax.domain.feedback.dto.FeedbackRes;
import dev.syntax.domain.feedback.service.FeedbackService;
import dev.syntax.domain.report.dto.ReportRes;
import dev.syntax.domain.report.service.ReportService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 용돈 계좌 관련 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/allowance")
@RequiredArgsConstructor
public class AllowanceController {

	private final BankAccountService bankAccountService;
	private final ReportService reportService;
	private final CardCreateService cardCreateService;
    private final FeedbackService feedbackService;

	/**
	 * 자녀의 용돈 계좌를 생성합니다.
	 * @param userContext 현재 사용자 정보
	 * @param req 자녀 계좌 생성 요청
	 * @return 성공 응답
	 */
	@PostMapping("/accounts")
	public ResponseEntity<?> createAccount(@CurrentUser UserContext userContext,
		@RequestBody CreateChildAccountReq req) {
		bankAccountService.createChildAllowanceAccount(userContext.getUser(), req);
		return ApiResponseUtil.success(SuccessCode.CREATED);
	}


    /**
     * 자녀 본인 리포트 조회
     * GET /allowance/report?year=2025&month=1
     */
    @GetMapping("/report")
    public ResponseEntity<BaseResponse<?>> getMyReport(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserContext ctx
    ) {
        log.info("[컸트롤러] 본인 리포트 조회 요청 - userId: {}, year: {}, month: {}", ctx.getId(), year, month);
        ReportRes result = reportService.getMonthlyReport(ctx.getId(), year, month, ctx);
        return ApiResponseUtil.success(SuccessCode.OK, result);
    }

    /**
     * 부모가 자녀 리포트 조회
     * GET /allowance/{childId}/report?year=2025&month=1
     */
    @GetMapping("/{childId}/report")
    public ResponseEntity<BaseResponse<?>> getChildReport(
            @PathVariable Long childId,
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserContext ctx
    ) {
        ReportRes result = reportService.getMonthlyReport(childId, year, month, ctx);
        return ApiResponseUtil.success(SuccessCode.OK, result);
    }

    /**
     * 📌 카드 발급 API
     * POST /allowance/cards
     */
    @PostMapping("/cards")
    public ResponseEntity<BaseResponse<?>> createCard(
            @CurrentUser UserContext ctx,
            @RequestBody CardCreateReq req) {

        CardInfoRes res = cardCreateService.createCard(req, ctx);

        return ApiResponseUtil.success(SuccessCode.CREATED, res);
    }

    /**
     * 리포트 피드백 생성
     * <p>
     * 부모가 자녀의 월간 리포트에 피드백(코멘트)을 작성합니다.
     * </p>
     *
     * @param ctx 로그인한 사용자 컨텍스트 (부모 권한 필요)
     * @param req 피드백 생성 요청 정보 (리포트 ID, 메시지)
     * @return 성공 응답 (201 Created)
     */
    @PostMapping("/feedback")
    public ResponseEntity<BaseResponse<?>> createFeedback(
            @AuthenticationPrincipal UserContext ctx,
            @RequestBody FeedbackCreateReq req) {

        FeedbackRes res = feedbackService.createFeedback(ctx, req);
        return ApiResponseUtil.success(SuccessCode.CREATED, res);
    }

    /**
     * 리포트 피드백 조회
     * <p>
     * 특정 리포트에 작성된 피드백을 조회합니다.
     * 자녀(본인) 또는 부모(연결된 자녀)만 조회 가능합니다.
     * </p>
     *
     * @param ctx      로그인한 사용자 컨텍스트
     * @param reportId 조회할 리포트 ID
     * @return 피드백 정보 (ID, 메시지)
     */
    @GetMapping("/feedback")
    public ResponseEntity<BaseResponse<?>> getFeedback(
        @AuthenticationPrincipal UserContext ctx,
        @RequestParam("reportId") Long reportId
    ) {
        var result = feedbackService.getFeedback(ctx, reportId);
        return ApiResponseUtil.success(SuccessCode.OK, result);
    }
}
