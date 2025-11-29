package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.req.InvestTradeOrderReq;
import dev.syntax.domain.investment.dto.res.InvestTradeOrderRes;
import dev.syntax.domain.investment.service.InvestAccountService;
import dev.syntax.domain.investment.service.InvestTradeOrderService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments/trade")
@Slf4j
public class InvestTradeOrderController {
    private final InvestTradeOrderService tradeOrderService;
    private final InvestAccountService investAccountService;

    @PostMapping("/buy")
    public ResponseEntity<BaseResponse<?>> buyStocks(
            @CurrentUser UserContext userContext,
            @RequestBody InvestTradeOrderReq buyReq
    ) {
        String cano = investAccountService.getCanoByUserId(userContext.getId());
        log.info("cano : {}", cano);
        InvestTradeOrderRes response = tradeOrderService.buy(userContext.getId(), cano, buyReq);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
    @PostMapping("/sell")
    public ResponseEntity<BaseResponse<?>> sellStocks(
            @CurrentUser UserContext userContext,
            @RequestBody InvestTradeOrderReq sellReq
    ) {
        String cano = investAccountService.getCanoByUserId(userContext.getId());
        InvestTradeOrderRes response = tradeOrderService.sell(userContext.getId(), cano, sellReq);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
}
