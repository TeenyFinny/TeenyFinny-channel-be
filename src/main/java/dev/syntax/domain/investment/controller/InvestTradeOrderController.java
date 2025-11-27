package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.req.BuyReq;
import dev.syntax.domain.investment.dto.req.SellReq;
import dev.syntax.domain.investment.dto.res.TradeOrderRes;
import dev.syntax.domain.investment.service.InvestTradeOrderService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments/trade")
public class InvestTradeOrderController {
    private final InvestTradeOrderService tradeOrderService;


    @PostMapping("/buy")
    public ResponseEntity<BaseResponse<?>> buyStocks(
            @RequestBody BuyReq buyReq
    ) {
        TradeOrderRes response = tradeOrderService.buy(buyReq);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
    @PostMapping("/sell")
    public ResponseEntity<BaseResponse<?>> sellStocks(
            @RequestBody SellReq sellReq
    ) {
        TradeOrderRes response = tradeOrderService.sell(sellReq);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
}
