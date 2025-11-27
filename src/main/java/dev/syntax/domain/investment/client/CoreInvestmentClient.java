package dev.syntax.domain.investment.client;

import dev.syntax.domain.account.dto.core.CoreInvestmentAccountRes;
import dev.syntax.domain.investment.dto.req.BuyReq;
import dev.syntax.domain.investment.dto.req.SellReq;
import dev.syntax.domain.investment.dto.res.InvestAccountPortfolioRes;
import dev.syntax.domain.investment.dto.res.InvestDashboardRes;
import dev.syntax.domain.investment.dto.res.StocksRes;
import dev.syntax.domain.investment.dto.res.TradeOrderRes;
import dev.syntax.global.core.CoreApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CoreInvestmentClient {
    private final RestTemplate coreRestTemplate;
    private final CoreApiProperties properties;

    private static final String STOCKS_URL = "/core/investments/stocks";
    private static final String INVEST_ACCOUNT_PORTFOLIO_URL = "/core/investments/account";
    private static final String INVESTMENT_ACCOUNT_URL = "/core/banking/account/investment";
    private static final String DASHBOARD_URL = "/core/investments/dashboard";
    private static final String TRADE_ORDER_BUY_URL = "/core/investments/trade/buy";
    private static final String TRADE_ORDER_SELL_URL = "/core/investments/trade/sell";


    public StocksRes getStocks() {
        return coreRestTemplate.getForObject(
                properties.getBaseUrl() + STOCKS_URL,
                StocksRes.class
        );
    }

    public InvestAccountPortfolioRes getInvestAccount(String cano) {
        return coreRestTemplate.getForObject(
                properties.getBaseUrl() + INVEST_ACCOUNT_PORTFOLIO_URL + "/" + cano,
                InvestAccountPortfolioRes.class
        );
    }


    public CoreInvestmentAccountRes createInvestmentAccount(Long userId) {
        return coreRestTemplate.postForObject(
                properties.getBaseUrl() + INVESTMENT_ACCOUNT_URL + "?userId=" + userId,
                null,
                CoreInvestmentAccountRes.class
        );
    }

    public InvestDashboardRes getDashboard(String cano) {
        return coreRestTemplate.getForObject(
                properties.getBaseUrl() + DASHBOARD_URL + "/" + cano,
                InvestDashboardRes.class
        );
    }

    public TradeOrderRes tradeOrderBuy(BuyReq buyReq) {
        return coreRestTemplate.postForObject(
                properties.getBaseUrl() + TRADE_ORDER_BUY_URL,
                buyReq,
                TradeOrderRes.class
        );
    }
    public TradeOrderRes tradeOrderSell(SellReq sellReq) {
        return coreRestTemplate.postForObject(
                properties.getBaseUrl() + TRADE_ORDER_SELL_URL,
                sellReq,
                TradeOrderRes.class
        );
    }

}
