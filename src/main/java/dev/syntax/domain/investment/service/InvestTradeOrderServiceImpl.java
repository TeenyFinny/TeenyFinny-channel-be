package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.client.CoreInvestmentClient;
import dev.syntax.domain.investment.dto.req.InvestTradeOrderReq;
import dev.syntax.domain.investment.dto.res.InvestTradeOrderRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestTradeOrderServiceImpl implements InvestTradeOrderService{
    private final CoreInvestmentClient coreInvestmentClient;

    @Override
    public InvestTradeOrderRes buy(InvestTradeOrderReq buyReq) {
        return coreInvestmentClient.tradeOrderBuy(buyReq);
    }

    @Override
    public InvestTradeOrderRes sell(InvestTradeOrderReq sellReq) {
        return coreInvestmentClient.tradeOrderSell(sellReq);
    }
}
