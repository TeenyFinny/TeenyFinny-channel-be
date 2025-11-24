package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.DashboardRes;
import dev.syntax.domain.investment.dto.res.PortfolioRes;
import dev.syntax.domain.investment.service.PortfolioService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments")
public class PortfolioController {
    PortfolioService portfolioService;

    @GetMapping("portfolio")
    public ResponseEntity<BaseResponse<PortfolioRes>> getPortfolio(
            @CurrentUser UserContext userContext
    ) {
        // TODO
        return null;
    }
}
