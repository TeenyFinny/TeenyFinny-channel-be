package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.InvestDashboardRes;
import dev.syntax.domain.investment.service.InvestDashboardService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments/dashboard")
@Slf4j
public class InvestDashboardController {
    private final InvestDashboardService dashboardService;

    @GetMapping
    public ResponseEntity<BaseResponse<?>> getDashboard(
        @CurrentUser UserContext userContext
    ) {
        InvestDashboardRes response = dashboardService.getDashboard(userContext);
        log.info("channel응답");
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
}
