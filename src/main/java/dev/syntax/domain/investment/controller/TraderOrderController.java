package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.req.BuyReq;
import dev.syntax.domain.investment.dto.req.SellReq;
import dev.syntax.domain.investment.dto.res.TradeOrderRes;
import dev.syntax.domain.investment.service.TradeOrderService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments/trade")
public class TraderOrderController {
    private final TradeOrderService tradeOrderService;

    @PostMapping("/buy")
    public ResponseEntity<BaseResponse<?>> buy(
            @CurrentUser UserContext userContext,
            BuyReq buyReq
    ) {
        TradeOrderRes response = tradeOrderService.buy(buyReq,userContext);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }

    @PostMapping("/sell")
    public ResponseEntity<BaseResponse<?>> buy(
            @CurrentUser UserContext userContext,
            SellReq sellReq
    ) {
        TradeOrderRes response = tradeOrderService.sell(sellReq,userContext);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
}
