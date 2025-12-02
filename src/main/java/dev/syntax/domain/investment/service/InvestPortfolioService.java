package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.PortfolioDateRes;
import dev.syntax.domain.investment.dto.res.PortfolioRes;
import java.util.List;

public interface InvestPortfolioService {
    PortfolioRes getPortfolio(String cano, int year, int month);

    List<PortfolioDateRes> getAvailableDates(String cano);
}
