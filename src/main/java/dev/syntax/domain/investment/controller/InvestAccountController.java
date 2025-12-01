package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.req.CreateInvestAccountReq;
import dev.syntax.domain.investment.dto.res.InvestAccountPortfolioRes;
import dev.syntax.domain.investment.service.InvestAccountService;
import dev.syntax.global.auth.annotation.CurrentUser;

import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import org.springframework.web.bind.annotation.RequestBody;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments/account")
public class InvestAccountController {
	private final InvestAccountService investAccountService;

	@GetMapping
	public ResponseEntity<BaseResponse<?>> getInvestAccountPortfolio (
			@CurrentUser UserContext userContext
	) {
		String cano = investAccountService.getCanoByUserId(userContext.getId());
		InvestAccountPortfolioRes res = investAccountService.getInvestAccount(cano);

		return ApiResponseUtil.success(SuccessCode.OK, res);
	}

	@PostMapping
	public ResponseEntity<BaseResponse<?>> createInvestmentAccount(
			@CurrentUser UserContext userContext,
            @RequestBody CreateInvestAccountReq req
	) {
        Long childId = req.childId();

        // 1) 부모인지 체크
        if (!"PARENT".equals(userContext.getRole())) {
            throw new BusinessException(ErrorBaseCode.FORBIDDEN); // 자녀는 계좌 개설 불가
        }

        // 2) 요청한 childId가 현재 부모의 자녀인지 검증
        if (!userContext.getChildren().contains(childId)) {
            throw new BusinessException(ErrorBaseCode.INVALID_CHILD);
        }
		investAccountService.createInvestmentAccount(childId);
		return ApiResponseUtil.success(SuccessCode.CREATED);
	}

	@GetMapping("/check-account")
    public ResponseEntity<BaseResponse<?>> checkAccount(
			@CurrentUser UserContext userContext
    ) {
        boolean hasAccount = investAccountService.checkAccount(userContext.getId());
        return ApiResponseUtil.success(SuccessCode.OK, hasAccount);
    }
}
