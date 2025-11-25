package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.StocksRes;
import dev.syntax.domain.investment.service.StocksService;
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
public class StocksController {
    private final StocksService stocksService;


    @GetMapping("/stocks")
    public ResponseEntity<BaseResponse<?>> getStocks() {
        StocksRes response = stocksService.getStocks();
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
}
