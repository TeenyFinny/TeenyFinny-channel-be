package dev.syntax.domain.investment.client;

import dev.syntax.domain.investment.dto.res.InvestDashboardRes;
import dev.syntax.domain.investment.dto.res.InvestAccountRes;
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
    private static final String INVEST_ACCOUNT_URL = "/core/investments/account";
    private static final String DASHBOARD_URL = "/core/investments/dashboard";

    public StocksRes getStocks() {
        return coreRestTemplate.getForObject(
                properties.getBaseUrl() + STOCKS_URL,
                StocksRes.class
        );
    }

    public InvestAccountRes getInvestAccount(String cano) {
        return coreRestTemplate.getForObject(
                properties.getBaseUrl() + INVEST_ACCOUNT_URL + "/" + cano,
                InvestAccountRes.class
        );
    }

    public InvestDashboardRes getDashboard(String cano) {
        return coreRestTemplate.getForObject(
                properties.getBaseUrl() + DASHBOARD_URL + "/" + cano,
                InvestDashboardRes.class
        );
    }
}
