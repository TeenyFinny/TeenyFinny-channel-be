package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.req.InvestTradeOrderReq;
import dev.syntax.domain.investment.dto.res.InvestTradeOrderRes;


public interface InvestTradeOrderService {
    public InvestTradeOrderRes buy(String cano, InvestTradeOrderReq buyReq);
    public InvestTradeOrderRes sell(String cano, InvestTradeOrderReq sellReq);
}
