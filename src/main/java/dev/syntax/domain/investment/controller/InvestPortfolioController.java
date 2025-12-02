package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.PortfolioDateRes;
import dev.syntax.domain.investment.dto.res.PortfolioRes;
import dev.syntax.domain.investment.service.InvestAccountService;
import dev.syntax.domain.investment.service.InvestPortfolioService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments/portfolio")
@Slf4j
public class InvestPortfolioController {
    private final InvestPortfolioService portfolioService;
    private final InvestAccountService investAccountService;

    @GetMapping
    public ResponseEntity<BaseResponse<?>> getPortfolio(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long childId,
            @CurrentUser UserContext userContext
    ) {
        Long targetUserId = (childId != null) ? childId : userContext.getId();
        String cano = investAccountService.getCanoByUserId(targetUserId);
        PortfolioRes res = portfolioService.getPortfolio(cano, year, month);
        return ApiResponseUtil.success(SuccessCode.OK, res);
    }


    @GetMapping("/dates")
    public ResponseEntity<?> getAvailableDates(
            @RequestParam(required = false) Long childId,
            @CurrentUser UserContext user
    ) {
        Long targetUserId = (childId != null) ? childId : user.getId();
        String cano = investAccountService.getCanoByUserId(targetUserId);

        List<PortfolioDateRes> dates = portfolioService.getAvailableDates(cano);

        log.info(dates.toString());
        return ApiResponseUtil.success(SuccessCode.OK, dates);
    }
}
