package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.req.InvestTradeOrderReq;
import dev.syntax.domain.investment.dto.res.InvestTradeOrderRes;
import dev.syntax.domain.investment.service.InvestAccountService;
import dev.syntax.domain.investment.service.InvestTradeOrderService;
import dev.syntax.global.auth.annotation.CurrentUser;
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
            @CurrentUser Long userId,
            @RequestBody InvestTradeOrderReq buyReq
    ) {
        String cano = investAccountService.getCanoByUserId(userId);
        log.info("cano : {}", cano);
        InvestTradeOrderRes response = tradeOrderService.buy(userId, cano, buyReq);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
    @PostMapping("/sell")
    public ResponseEntity<BaseResponse<?>> sellStocks(
            @CurrentUser Long userId,
            @RequestBody InvestTradeOrderReq sellReq
    ) {
        String cano = investAccountService.getCanoByUserId(userId);
        InvestTradeOrderRes response = tradeOrderService.sell(userId, cano, sellReq);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
}
