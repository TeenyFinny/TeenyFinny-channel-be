package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.client.CoreInvestmentClient;
import dev.syntax.domain.investment.dto.req.BuyReq;
import dev.syntax.domain.investment.dto.req.SellReq;
import dev.syntax.domain.investment.dto.res.TradeOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestTradeOrderServiceImpl implements InvestTradeOrderService{
    private final CoreInvestmentClient coreInvestmentClient;

    @Override
    public TradeOrder buy(BuyReq buyReq) {
        return coreInvestmentClient.tradeOrderBuy(buyReq);
    }

    @Override
    public TradeOrder sell(SellReq sellReq ) {
        return coreInvestmentClient.tradeOrderSell(sellReq);
    }
}
