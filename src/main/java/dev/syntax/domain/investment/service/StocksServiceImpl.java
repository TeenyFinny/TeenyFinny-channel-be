package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.client.CoreInvestmentClient;
import dev.syntax.domain.investment.dto.res.StockDetailRes;
import dev.syntax.domain.investment.dto.res.StocksRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StocksServiceImpl implements StocksService{
    private final CoreInvestmentClient coreInvestmentClient;

    @Override
    public StocksRes getStocksForBuy() {
        // Core API 호출 - 에러 처리 자동
        StocksRes res = coreInvestmentClient.getStocksForBuy();

        return res;
    }

    @Override
    public StocksRes getStocksForSell() {
        // Core API 호출 - 에러 처리 자동
        StocksRes res = coreInvestmentClient.getStocksForSell();

        return res;
    }


    @Override
    public StockDetailRes getStockDetail(String code) {
        return coreInvestmentClient.getStockDetail(code);
    }
}
