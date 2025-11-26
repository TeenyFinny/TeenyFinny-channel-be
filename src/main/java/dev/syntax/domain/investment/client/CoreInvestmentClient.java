package dev.syntax.domain.investment.client;

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

    private final static String STOCKS_URL = "/core/investments/stocks";
    private final static String INVEST_ACCOUNT_URL = "/core/investments/account";

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
}
