package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.client.CoreInvestmentClient;
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
    public StocksRes getStocks() {
        // Core API 호출 - 에러 처리 자동
        StocksRes res = coreInvestmentClient.getStocks();

        return res;
    }

    @Override
    public StocksRes getStock(String code) {
        StocksRes res = coreInvestmentClient.getStock(code);
        return res;
    }
}
