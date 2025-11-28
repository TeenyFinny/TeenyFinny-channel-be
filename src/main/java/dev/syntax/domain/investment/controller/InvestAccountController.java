package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.InvestAccountPortfolioRes;
import dev.syntax.domain.investment.dto.res.InvestAccountRes;
import dev.syntax.domain.investment.service.InvestAccountService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments/account")
public class InvestAccountController {
	private final InvestAccountService investAccountService;

	@GetMapping
	public ResponseEntity<BaseResponse<?>> getInvestAccountPortfolio (
			@CurrentUser Long userId
	) {
		String cano = investAccountService.getCanoByUserId(userId);
		InvestAccountPortfolioRes res = investAccountService.getInvestAccount(cano);

		return ApiResponseUtil.success(SuccessCode.OK, res);
	}

	@PostMapping
	public ResponseEntity<BaseResponse<?>> createInvestmentAccount(
			@CurrentUser Long userId
	) {
		InvestAccountRes res = investAccountService.createInvestmentAccount(userId);
		return ApiResponseUtil.success(SuccessCode.CREATED, res);
	}

	@GetMapping("/check-account")
    public ResponseEntity<BaseResponse<?>> checkAccount(
            @CurrentUser Long userId
    ) {
        boolean hasAccount = investAccountService.checkAccount(userId);
        return ApiResponseUtil.success(SuccessCode.OK, hasAccount);
    }
}
