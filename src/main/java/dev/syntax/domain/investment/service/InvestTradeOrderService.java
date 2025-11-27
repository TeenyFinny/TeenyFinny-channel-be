package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.req.BuyReq;
import dev.syntax.domain.investment.dto.req.SellReq;
import dev.syntax.domain.investment.dto.res.TradeOrder;


public interface InvestTradeOrderService {
    public TradeOrder buy(BuyReq buyReq);
    public TradeOrder sell(SellReq sellReq);
}
