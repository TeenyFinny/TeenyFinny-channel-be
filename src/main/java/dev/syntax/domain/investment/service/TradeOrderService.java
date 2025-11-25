package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.req.BuyReq;
import dev.syntax.domain.investment.dto.req.SellReq;
import dev.syntax.domain.investment.dto.res.TradeOrderRes;
import dev.syntax.global.auth.dto.UserContext;
import org.springframework.stereotype.Service;

@Service
public interface TradeOrderService {
    public TradeOrderRes buy(BuyReq buyReq, UserContext userContext);
    public TradeOrderRes sell(SellReq sellReq, UserContext userContext);
}
