package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.InvestAccountRes;
import dev.syntax.domain.investment.service.InvestAccountService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments")
public class InvestAccountController {
	private final InvestAccountService investAccountService;

	@GetMapping("/account")
	public ResponseEntity<BaseResponse<?>> getInvestAccount (
					@CurrentUser UserContext userContext
	) {
		String cano = investAccountService.getCanoByUserId(userContext.getId());
		InvestAccountRes res = investAccountService.getInvestAccount(cano);

		return ApiResponseUtil.success(SuccessCode.OK, res);
	}
}
