package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.req.BuyReq;
import dev.syntax.domain.investment.dto.req.SellReq;
import dev.syntax.domain.investment.dto.res.TradeOrderRes;


public interface InvestTradeOrderService {
    public TradeOrderRes buy(BuyReq buyReq);
    public TradeOrderRes sell(SellReq sellReq);
}
