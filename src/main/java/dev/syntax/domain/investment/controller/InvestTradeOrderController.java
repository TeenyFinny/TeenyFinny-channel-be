package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.req.InvestTradeOrderReq;
import dev.syntax.domain.investment.dto.res.InvestTradeOrderRes;
import dev.syntax.domain.investment.service.InvestTradeOrderService;
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
            @RequestBody InvestTradeOrderReq buyReq
    ) {
        InvestTradeOrderRes response = tradeOrderService.buy(buyReq);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
    @PostMapping("/sell")
    public ResponseEntity<BaseResponse<?>> sellStocks(
            @RequestBody InvestTradeOrderReq sellReq
    ) {
        InvestTradeOrderRes response = tradeOrderService.sell(sellReq);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
}
