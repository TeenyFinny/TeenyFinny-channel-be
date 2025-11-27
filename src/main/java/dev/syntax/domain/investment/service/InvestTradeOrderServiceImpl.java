package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.client.CoreInvestmentClient;
import dev.syntax.domain.investment.dto.req.BuyReq;
import dev.syntax.domain.investment.dto.req.SellReq;
import dev.syntax.domain.investment.dto.res.TradeOrderRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestTradeOrderServiceImpl implements InvestTradeOrderService{
    private final CoreInvestmentClient coreInvestmentClient;

    @Override
    public TradeOrderRes buy(BuyReq buyReq) {
        return coreInvestmentClient.tradeOrderBuy(buyReq);
    }

    @Override
    public TradeOrderRes sell(SellReq sellReq) {
        return coreInvestmentClient.tradeOrderSell(sellReq);
    }
}
