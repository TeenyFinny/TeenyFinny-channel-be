package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.client.CoreInvestmentClient;
import dev.syntax.domain.investment.dto.res.PortfolioDateRes;
import dev.syntax.domain.investment.dto.res.PortfolioRes;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements InvestPortfolioService {
    private final CoreInvestmentClient coreInvestmentClient;
    @Override
    public PortfolioRes getPortfolio(String cano, int year, int month) {

        return coreInvestmentClient.getMonthlyPortfolio(cano, year, month);
    }

    @Override
    public List<PortfolioDateRes> getAvailableDates(String cano) {
        return coreInvestmentClient.getAvailableDates(cano);
    }
}
