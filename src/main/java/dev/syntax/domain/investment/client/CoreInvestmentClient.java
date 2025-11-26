package dev.syntax.domain.investment.client;

import dev.syntax.domain.account.dto.core.CoreInvestmentAccountRes;
import dev.syntax.domain.investment.dto.res.InvestAccountPortfolioRes;
import dev.syntax.domain.investment.dto.res.StocksRes;
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
}
