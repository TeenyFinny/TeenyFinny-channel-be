package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.StockDetailRes;
import dev.syntax.domain.investment.dto.res.StocksRes;

public interface StocksService {
    StocksRes getStocksForSell();
    StocksRes getStocksForBuy();
    StockDetailRes getStockDetail(String code);
}
