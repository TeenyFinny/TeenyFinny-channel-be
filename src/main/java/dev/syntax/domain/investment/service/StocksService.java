package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.StocksRes;

public interface StocksService {
    StocksRes getStocks();
    StocksRes getStock(String code);
}
