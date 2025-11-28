package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.req.CreateInvestAccountReq;
import dev.syntax.domain.investment.dto.res.InvestAccountPortfolioRes;
import dev.syntax.domain.investment.dto.res.InvestAccountRes;
import dev.syntax.domain.investment.service.InvestAccountService;
import dev.syntax.global.auth.annotation.CurrentUser;

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
		InvestAccountRes res = investAccountService.createInvestmentAccount(req.childId());
		return ApiResponseUtil.success(SuccessCode.CREATED, res);
	}
}
