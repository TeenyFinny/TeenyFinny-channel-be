package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.PortfolioRes;
import dev.syntax.domain.investment.service.InvestAccountService;
import dev.syntax.domain.investment.service.InvestPortfolioService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments/portfolio")
public class InvestPortfolioController {
    private final InvestPortfolioService portfolioService;
    private final InvestAccountService investAccountService;

    @GetMapping
    public ResponseEntity<BaseResponse<?>> getPortfolio(
            @RequestParam int year,
            @RequestParam int month,
            @CurrentUser UserContext userContext
    ) {
        String cano = investAccountService.getCanoByUserId(userContext.getId());
        PortfolioRes res = portfolioService.getPortfolio(cano, year, month);
        return ApiResponseUtil.success(SuccessCode.OK, res);
    }
}
