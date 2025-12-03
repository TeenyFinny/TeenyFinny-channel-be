package dev.syntax.domain.investment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.syntax.domain.investment.dto.res.StockDetailRes;
import dev.syntax.domain.investment.dto.res.StocksRes;
import dev.syntax.domain.investment.service.StocksService;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments/stocks")
@Slf4j
public class StocksController {
    private final StocksService stocksService;


    @GetMapping("/buy")
    public ResponseEntity<BaseResponse<?>> getStocksForBuy() {
        StocksRes response = stocksService.getStocksForBuy();
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }

    @GetMapping("/sell")
    public ResponseEntity<BaseResponse<?>> getStocksForSell() {
        StocksRes response = stocksService.getStocksForSell();
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }

    @GetMapping("/detail/{code}")
    public ResponseEntity<BaseResponse<?>> getStockDetail(
            @PathVariable String code
    ) {
        StockDetailRes response = stocksService.getStockDetail(code);
        log.info(response.toString());
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
}
