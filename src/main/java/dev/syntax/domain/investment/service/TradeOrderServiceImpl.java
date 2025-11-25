package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.req.BuyReq;
import dev.syntax.domain.investment.dto.req.SellReq;
import dev.syntax.domain.investment.dto.res.TradeOrderRes;
import dev.syntax.global.auth.dto.UserContext;

public class TradeOrderServiceImpl implements TradeOrderService{
    private final CoreInvestmentClient coreInvestmentClient;
    AccountService accountService;

    @Override
    public TradeOrderRes buy(BuyReq buyReq, UserContext userContext) {
        String cano = accountService.getCanoByUserId(userContext.getId());

        return coreInvestmentClient.buy(buyReq, cano);
    }

    @Override
    public TradeOrderRes sell(SellReq sellReq, UserContext userContext) {
        String cano = accountService.getCanoByUserId(userContext.getId());

        return coreInvestmentClient.sell(sellReq, cano);
    }
}
