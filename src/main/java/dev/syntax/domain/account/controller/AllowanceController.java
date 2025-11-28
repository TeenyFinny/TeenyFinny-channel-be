package dev.syntax.domain.account.controller;

import dev.syntax.domain.account.dto.CreateChildAccountReq;
import dev.syntax.domain.account.service.BankAccountService;
import dev.syntax.domain.card.dto.CardCreateReq;
import dev.syntax.domain.card.dto.CardInfoRes;
import dev.syntax.domain.card.service.CardCreateService;
import dev.syntax.domain.report.dto.ReportRes;
import dev.syntax.domain.report.service.ReportService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
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
@RestController
@RequestMapping("/allowance")
@RequiredArgsConstructor
public class AllowanceController {

	private final BankAccountService bankAccountService;
	private final ReportService reportService;
	private final CardCreateService cardCreateService;

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
     * GET /allowance/report?month=1
     */
    @GetMapping("/report")
    public ResponseEntity<BaseResponse<?>> getMyReport(
            @RequestParam int month,
            @AuthenticationPrincipal UserContext ctx
    ) {
        ReportRes result = reportService.getMonthlyReport(ctx.getId(), month, ctx);
        return ApiResponseUtil.success(SuccessCode.OK, result);
    }

    /**
     * 부모가 자녀 리포트 조회
     * GET /allowance/{childId}/report?month=1
     */
    @GetMapping("/{childId}/report")
    public ResponseEntity<BaseResponse<?>> getChildReport(
            @PathVariable Long childId,
            @RequestParam int month,
            @AuthenticationPrincipal UserContext ctx
    ) {
        ReportRes result = reportService.getMonthlyReport(childId, month, ctx);
        return ApiResponseUtil.success(SuccessCode.OK, result);
    }

    /**
     * 📌 카드 발급 API
     * POST /allowance/card
     */
    @PostMapping("/cards")
    public ResponseEntity<BaseResponse<?>> createCard(
            @CurrentUser UserContext ctx,
            @RequestBody CardCreateReq req) {

        CardInfoRes res = cardCreateService.createCard(req, ctx);

        return ApiResponseUtil.success(SuccessCode.CREATED, res);
    }
}
