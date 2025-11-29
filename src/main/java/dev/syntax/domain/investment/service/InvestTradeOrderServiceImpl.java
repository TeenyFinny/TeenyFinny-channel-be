package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.client.CoreInvestmentClient;
import dev.syntax.domain.investment.dto.core.CoreInvestTradeOrderReq;
import dev.syntax.domain.investment.dto.req.InvestTradeOrderReq;
import dev.syntax.domain.investment.dto.res.InvestTradeOrderRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestTradeOrderServiceImpl implements InvestTradeOrderService{
    private final CoreInvestmentClient coreInvestmentClient;

    @Override
    public InvestTradeOrderRes buy(String cano, InvestTradeOrderReq req) {
        return coreInvestmentClient.tradeOrderBuy(
                new CoreInvestTradeOrderReq(cano, req.getProductCode(), req.getProductName(),
                        req.getQuantity(), req.getPrice())
        );
    }

    @Override
    public InvestTradeOrderRes sell(String cano, InvestTradeOrderReq req) {
        return coreInvestmentClient.tradeOrderSell(
                new CoreInvestTradeOrderReq(cano, req.getProductCode(), req.getProductName(),
                        req.getQuantity(), req.getPrice())
        );
    }
}
