package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.DashboardRes;
import dev.syntax.domain.investment.service.DashboardService;
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
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<BaseResponse<?>> getDashboard(
            @CurrentUser UserContext userContext
    ) {
        DashboardRes response = dashboardService.getDashboard(userContext);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
}
