package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.req.InvestTradeOrderReq;
import dev.syntax.domain.investment.dto.res.InvestTradeOrderRes;


public interface InvestTradeOrderService {
    public InvestTradeOrderRes buy(Long userId, String cano, InvestTradeOrderReq buyReq);
    public InvestTradeOrderRes sell(Long userId, String cano, InvestTradeOrderReq sellReq);
}
